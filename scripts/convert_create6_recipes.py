#!/usr/bin/env python3
"""Convert Create 5 / pre-6 recipe JSON stacks from {\"item\": ...} to Create 6 {\"id\", \"count\"}."""

from __future__ import annotations

import json
import sys
import zipfile
from pathlib import Path
from typing import Any


def is_item_stack(obj: Any) -> bool:
    return isinstance(obj, dict) and "item" in obj and "id" not in obj and "type" not in obj


def convert_stack(obj: dict[str, Any]) -> dict[str, Any]:
    converted: dict[str, Any] = {}
    if "chance" in obj:
        converted["chance"] = obj["chance"]
    converted["id"] = obj["item"]
    count = obj.get("count", 1)
    if count != 1:
        converted["count"] = count
    return converted


def convert_node(node: Any) -> Any:
    if isinstance(node, list):
        return [convert_node(item) for item in node]
    if not isinstance(node, dict):
        return node

    if is_item_stack(node):
        return convert_stack(node)

    result = {}
    for key, value in node.items():
        if key == "transitionalItem":
            key = "transitional_item"
        if key == "key" and isinstance(value, dict):
            result[key] = {k: convert_node(v) for k, v in value.items()}
            continue
        if key == "result" and is_item_stack(value):
            result[key] = convert_stack(value)
            continue
        if key in {"ingredient", "transitional_item"} and is_item_stack(value):
            result[key] = convert_stack(value)
            continue
        if key in {"ingredients", "results", "sequence"}:
            result[key] = convert_node(value)
            continue
        result[key] = convert_node(value)
    return result


def convert_file(path: Path) -> None:
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    path.write_text(json.dumps(convert_node(data), indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def convert_tree(root: Path) -> int:
    count = 0
    for path in sorted(root.rglob("*.json")):
        convert_file(path)
        count += 1
    return count


def extract_and_convert_from_jar(jar: Path, recipe_prefix: str, out_root: Path, recipe_paths: list[str]) -> int:
    out_root.mkdir(parents=True, exist_ok=True)
    count = 0
    with zipfile.ZipFile(jar) as archive:
        for recipe_path in recipe_paths:
            try:
                raw = archive.read(recipe_path)
            except KeyError:
                print(f"Missing in jar: {recipe_path}", file=sys.stderr)
                continue
            data = json.loads(raw.decode("utf-8"))
            converted = convert_node(data)
            target = out_root / recipe_path
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(json.dumps(converted, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
            count += 1
    return count


FAILED_CBCM_RECIPES = [
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_reductive_short_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/dual_he_rocket.json",
    "data/cbcmoreshells/recipe/dual_aphe_rocket.json",
    "data/cbcmoreshells/recipe/torpedo_components/slow_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/highspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/light_high_speed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/gambler_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/medium_range_deepwater_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_short_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reductive_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reductive_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/torpedo_head.json",
    "data/cbcmoreshells/recipe/torpedo_components/early_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_reductive_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/gambler_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/highspeed_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reductive_highspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_torpedo_head.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/early_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_short_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/ultraspeed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_reductive_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/highspeed_long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/highspeed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/ultraspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_reductive_short_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/primary_torpedo_mold.json",
    "data/cbcmoreshells/recipe/deployer/normal_ap.json",
]


def main() -> None:
    repo = Path(__file__).resolve().parents[1]
    workspace = repo.parent

    mod_recipe_root = repo / "src/main/resources/data/createnucleararmaments/recipe"
    mod_count = convert_tree(mod_recipe_root)

    fissile = mod_recipe_root / "mechanical_crafting/fissile_precursor.json"
    data = json.loads(fissile.read_text(encoding="utf-8-sig"))
    data["pattern"] = [line.replace("U", "H") for line in data["pattern"]]
    data = convert_node(data)
    fissile.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    armament = mod_recipe_root / "mechanical_crafting/armament_uranium_billet.json"
    convert_file(armament)

    datapack_root = workspace / "cbcmoreshells-create6-recipe-fix"
    pack_meta = datapack_root / "pack.mcmeta"
    pack_meta.parent.mkdir(parents=True, exist_ok=True)
    pack_meta.write_text(
        json.dumps(
            {
                "pack": {
                    "description": "Create 6 recipe format fixes for CBC Military Supplement",
                    "pack_format": 48,
                    "supported_formats": {"min_inclusive": 48, "max_inclusive": 81},
                }
            },
            indent=2,
        )
        + "\n",
        encoding="utf-8",
    )

    jar = repo / "libs/CBC-Military-Supplement-1.21.1-2.1.0.jar"
    dp_count = extract_and_convert_from_jar(jar, "data/cbcmoreshells/recipe/", datapack_root, FAILED_CBCM_RECIPES)

    readme = datapack_root / "README.txt"
    readme.write_text(
        """CBC Military Supplement — Create 6 recipe fix datapack

Install:
  - Copy this folder into your world's datapacks directory, or
  - For dev client: copy into create-nuclear-armaments/run/datapacks/

Fixes 36 recipes that failed JSON parsing under Create 6.0+ by converting
legacy {"item": "..."} stacks to {"id": "...", "count": 1} and renaming
transitionalItem -> transitional_item.

Not fixable via datapack (require More Shells mod update):
  - cbcmoreshells:shell_fuzing
  - cbcmoreshells:shell_fuzing_deployer
  These use custom recipe serializers not registered on 1.21.1 NeoForge.
""",
        encoding="utf-8",
    )

    run_dp = repo / "run/datapacks/cbcmoreshells-create6-recipe-fix"
    if run_dp.exists():
        import shutil

        shutil.rmtree(run_dp)
    import shutil

    shutil.copytree(datapack_root, run_dp)

    print(f"Converted {mod_count} mod recipe files under {mod_recipe_root.relative_to(repo)}")
    print(f"Wrote {dp_count} datapack recipe overrides to {datapack_root}")
    print(f"Copied datapack to {run_dp}")


if __name__ == "__main__":
    main()
