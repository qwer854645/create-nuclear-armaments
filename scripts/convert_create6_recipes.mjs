import fs from "node:fs";
import path from "node:path";
import { fileURLToPath } from "node:url";
import { execFileSync } from "node:child_process";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repo = path.resolve(__dirname, "..");
const workspace = path.resolve(repo, "..");

function isItemStack(obj) {
  return obj && typeof obj === "object" && !Array.isArray(obj) && "item" in obj && !("id" in obj) && !("type" in obj);
}

function convertStack(obj) {
  const converted = {};
  if ("chance" in obj) converted.chance = obj.chance;
  converted.id = obj.item;
  const count = obj.count ?? 1;
  if (count !== 1) converted.count = count;
  return converted;
}

function convertNode(node) {
  if (Array.isArray(node)) return node.map(convertNode);
  if (!node || typeof node !== "object") return node;
  if (isItemStack(node)) return convertStack(node);

  const result = {};
  for (const [rawKey, value] of Object.entries(node)) {
    const key = rawKey === "transitionalItem" ? "transitional_item" : rawKey;
    result[key] = convertNode(value);
  }
  return result;
}

function readJson(filePath) {
  const raw = fs.readFileSync(filePath, "utf8").replace(/^\uFEFF/, "");
  return JSON.parse(raw);
}

function convertFile(filePath) {
  const data = readJson(filePath);
  const converted = convertNode(data);
  if (filePath.endsWith("fissile_precursor.json") && Array.isArray(converted.pattern)) {
    converted.pattern = converted.pattern.map((line) => line.replaceAll("U", "H"));
  }
  fs.writeFileSync(filePath, JSON.stringify(converted, null, 2) + "\n", "utf8");
}

function walkJson(root) {
  let count = 0;
  for (const entry of fs.readdirSync(root, { withFileTypes: true })) {
    const full = path.join(root, entry.name);
    if (entry.isDirectory()) count += walkJson(full);
    else if (entry.name.endsWith(".json")) {
      convertFile(full);
      count++;
    }
  }
  return count;
}

const FAILED_CBCM_RECIPES = [
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
];

const modRecipeRoot = path.join(repo, "src/main/resources/data/createnucleararmaments/recipe");
const modCount = walkJson(modRecipeRoot);

const datapackRoot = path.join(workspace, "cbcmoreshells-create6-recipe-fix");
const jar = path.join(repo, "libs/CBC-Military-Supplement-1.21.1-2.1.0.jar");
const tempExtract = path.join(repo, "build/tmp/cbcm-recipe-extract");
fs.rmSync(tempExtract, { recursive: true, force: true });
fs.mkdirSync(tempExtract, { recursive: true });

execFileSync(
  "C:\\Program Files\\Java\\jdk-21.0.10\\bin\\jar.exe",
  ["xf", jar, ...FAILED_CBCM_RECIPES],
  { cwd: tempExtract, stdio: "inherit" }
);

let dpCount = 0;
for (const recipePath of FAILED_CBCM_RECIPES) {
  const source = path.join(tempExtract, recipePath);
  if (!fs.existsSync(source)) {
    console.warn("Missing:", recipePath);
    continue;
  }
  const data = readJson(source);
  const converted = convertNode(data);
  const target = path.join(datapackRoot, recipePath);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, JSON.stringify(converted, null, 2) + "\n", "utf8");
  dpCount++;
}

fs.writeFileSync(
  path.join(datapackRoot, "pack.mcmeta"),
  JSON.stringify(
    {
      pack: {
        description: "Create 6 recipe format fixes for CBC Military Supplement",
        pack_format: 48,
        supported_formats: { min_inclusive: 48, max_inclusive: 81 },
      },
    },
    null,
    2
  ) + "\n"
);

fs.writeFileSync(
  path.join(datapackRoot, "README.txt"),
  `CBC Military Supplement — Create 6 recipe fix datapack

Install:
  - Copy this folder into your world's datapacks directory, or
  - For dev client: copy into create-nuclear-armaments/run/datapacks/

Fixes 36 recipes that failed JSON parsing under Create 6.0+.

Not fixable via datapack (require More Shells mod update):
  - cbcmoreshells:shell_fuzing
  - cbcmoreshells:shell_fuzing_deployer
`,
  "utf8"
);

const runDp = path.join(repo, "run/datapacks/cbcmoreshells-create6-recipe-fix");
fs.rmSync(runDp, { recursive: true, force: true });
fs.cpSync(datapackRoot, runDp, { recursive: true });

console.log(`Converted ${modCount} mod recipe files`);
console.log(`Wrote ${dpCount} datapack overrides to ${datapackRoot}`);
console.log(`Copied datapack to ${runDp}`);
