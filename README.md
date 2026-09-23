# Create Nuclear Armaments

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1-orange.svg)](https://neoforged.net/)
[![Latest](https://img.shields.io/github/v/release/qwer854645/create-nuclear-armaments)](https://github.com/qwer854645/create-nuclear-armaments/releases/latest)

Nuclear munitions for the Create ecosystem on **Minecraft 1.21.1 / NeoForge**.

Craft placeable nuclear charges and optional big-cannon munitions (shells, bombs, rockets, torpedoes). Detonations produce a mushroom-cloud presentation, cratering, edge fracture, terrain fallout, and optional radiation from **Create Nuclear** and/or **Create: New Age**.

> **AI involvement**  
> Parts of this project were developed with assistance from AI coding tools (Cursor). Human authors review, test, and remain responsible for the released code.

## Features

- **Placeable charges T1–T3** — redstone long fuse (~30s) or TNT / blast-chain short fuse (~5s); flint-and-steel does nothing
- **Optional CBC / CBCMS munitions T1–T3** — shells (Create Big Cannons), bombs / rockets / torpedoes (CBC Military Supplement)
- **Detonation FX** — rising mushroom column, shock skirt, edge fracture, fallout terrain conversion
- **Radiation soft hooks**
  - Create Nuclear: radiation effect when that mod is present
  - Create: New Age: `radiation_poisoning` plus a New Age radiation pulse (respects hazmat / shielding)
- **Crafting paths**
  - Create Nuclear feedstock → HEU compound → sequenced munition assembly
  - Alternate Create: New Age recipes (thorium, nuclear fuel, overcharged metals, circuits) when that mod is installed
- **Create-style Shift tooltips** with yield / crater / radiation stats
- **Configurable fallout lava** — `fallout.stoneLavaChance` in the server config (default `0.0005`)

## Dependencies

| Mod | Required? | Role |
|-----|-----------|------|
| Minecraft 1.21.1 + NeoForge | **Yes** | Runtime |
| [Create](https://modrinth.com/mod/create) 6.x | **Yes** | Hard dependency (CreateRegistrate, recipes, tooltips) |
| [Create Nuclear](https://modrinth.com/mod/create-nuclear) | Optional | HEU mixing ingredients + radiation effect |
| [Create Big Cannons](https://modrinth.com/mod/create-big-cannons) | Optional | Nuclear shells |
| [CBC More Shells](https://modrinth.com/mod/cbc-more-shells) (Military Supplement) | Optional | Nuclear bombs, rockets, torpedoes |
| [Create: New Age](https://modrinth.com/mod/create-new-age) | Optional | Alternate HEU / munition recipes + radiation poisoning (needs **ESL** when installed) |

**Soft loading:** With Create alone you still get placeable charges and full blast FX. CBC / CBCMS munitions register only when those mods are present. HEU registers if **either** Create Nuclear or Create: New Age is present.

## Gameplay notes

1. CBC-family projectiles need a proper **fuze** to detonate as intended.
2. Bedrock-tier unbreakable blocks survive; the blast core can clear obsidian-tier blocks.
3. Large yields are expensive — expect lag and a busy sound pool on high tiers.
4. Early **0.1.x** — balance and APIs may change. Prefer a test world.

## Download

GitHub Releases: [v0.1.4](https://github.com/qwer854645/create-nuclear-armaments/releases/tag/v0.1.4) · [latest](https://github.com/qwer854645/create-nuclear-armaments/releases/latest)

## Building

```bat
gradlew.bat build
```

Output: `build/libs/createnucleararmaments-<version>.jar`

Dev client (copies Create stack + optional jars from `libs` / `devRuntimeMods` into `run/mods`):

```bat
gradlew.bat runClient
```

Create-only bare client (skips optional addon jars):

```bat
gradlew.bat runClient -PbareClient
```

## License

[MIT](LICENSE). Third-party mods remain under their own licenses; this repository does not redistribute those jars.

## Credits

- Create, Create Nuclear, Create Big Cannons, CBC More Shells, and Create: New Age authors and communities
- Contributors to Create Nuclear Armaments

---

## 中文摘要

面向 Create 的核武扩展（MC 1.21.1 / NeoForge）：可放置核装置，以及可选的 CBC / CBCMS 炮弹、炸弹、火箭、鱼雷。爆炸含蘑菇云、沉降地形；可选接入 Create Nuclear 与 Create: New Age 的辐射。装有 New Age 时额外加载其材料配方。沉降岩浆概率由服务端配置 `fallout.stoneLavaChance` 控制（默认 `0.0005`）。

本项目部分内容由 AI 编程工具（Cursor）辅助完成；由人工审核、测试，并对发布代码负责。

**Version:** 0.1.4 · **Loader:** NeoForge · **MC:** 1.21.1
