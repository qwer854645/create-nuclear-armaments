# Create Nuclear Armaments

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1-orange.svg)](https://neoforged.net/)
[![Latest](https://img.shields.io/github/v/release/qwer854645/create-nuclear-armaments)](https://github.com/qwer854645/create-nuclear-armaments/releases/latest)

Weapons-grade nuclear munitions for the Create ecosystem — placeable charges plus optional CBC / CBCMS shells, bombs, rockets, and torpedoes — with mushroom-cloud FX, blast fallout, and optional Create Nuclear radiation hooks.

面向 Create 生态的核武器扩展：可放置核装置，以及可选的 CBC / CBCMS 核炮弹 / 核炸弹 / 核火箭 / 核鱼雷；含蘑菇云特效、冲击沉降，以及可选的 Create Nuclear 辐射兼容。

> **AI involvement / AI 参与说明**  
> Parts of this project were developed with assistance from AI coding tools (Cursor). Human authors review, test, and remain responsible for the released code.  
> 本项目部分内容由 AI 编程工具（Cursor）辅助完成；由人工审核、测试，并对发布代码负责。

## Features / 特性

- Placeable nuclear charges **T1–T3** (redstone fuse / TNT & blast-chain ignition)  
  可放置核装置 **T1–T3**（红石引信 / TNT 与爆炸连锁点燃）
- Optional CBC / CBCMS munitions **T1–T3**: shells, bombs, rockets, torpedoes  
  可选 CBC / CBCMS 弹药 **T1–T3**：炮弹、炸弹、火箭、鱼雷
- Detonation FX: rising mushroom column, shock skirt with falling smoke, edge fracture, fallout terrain  
  起爆表现：上升烟柱、冲击烟圈与下坠烟幕、边缘碎裂、沉降地形改造
- Create-style Shift tooltips (`ItemDescription` + yield stats)  
  Create 风格 Shift 提示（`ItemDescription` + 核当量数据）
- Soft content: CBC / CBCMS / Create Nuclear register only when those mods are present  
  软内容：仅在 CBC / CBCMS / Create Nuclear 存在时注册对应物品与配方

## Dependencies / 依赖

| Mod | Required? | Notes |
|-----|-----------|--------|
| Minecraft 1.21.1 + NeoForge | **Yes** | — |
| [Create](https://modrinth.com/mod/create) 6.x | **Yes** | Hard dependency; uses CreateRegistrate |
| [Create Nuclear](https://modrinth.com/mod/create-nuclear) | Optional | Feedstock items + radiation hooks |
| [Create Big Cannons](https://modrinth.com/mod/create-big-cannons) | Optional | Nuclear shells |
| [CBC More Shells](https://modrinth.com/mod/cbc-more-shells) (Military Supplement) | Optional | Nuclear bombs / rockets / torpedoes |

With Create alone you still get placeable charges and detonations. Without CBC / CBCMS the cannon-family munitions are skipped; without Create Nuclear the uranium feedstock items are skipped.

仅装 Create 时仍可使用放置核装置与爆炸效果。无 CBC / CBCMS 不注册火炮系核弹药；无 Create Nuclear 不注册核材料物品。

## Important notes / 注意事项

1. CBC-family munitions need a proper **fuze** to detonate as designed.  
   CBC 系弹药需正确安装**引信**。
2. Placeable charges: redstone uses a long fuse (~30s); TNT / blast chain uses a shorter fuse (~5s). Flint-and-steel will not ignite them.  
   放置核装置：红石长引信；TNT / 爆炸连锁为短引信；打火石无效。
3. Bedrock-tier unbreakable blocks are not destroyed; the blast core can clear obsidian-tier blocks.  
   基岩等不可破坏方块保留；内核可清掉黑曜石级方块。
4. High-yield blasts are heavy on performance and may exhaust the sound pool.  
   大当量爆炸可能卡顿并打满音效池。
5. Early **0.1.x** — expect balance and API changes. Prefer a test world.  
   早期 **0.1.x**，数值与内容可能大幅调整，建议测试世界。

## Download / 下载

GitHub Releases: [v0.1.2](https://github.com/qwer854645/create-nuclear-armaments/releases/tag/v0.1.2) · [latest](https://github.com/qwer854645/create-nuclear-armaments/releases/latest)

## Building / 构建

```bat
gradlew.bat build
```

Output jar: `build/libs/createnucleararmaments-<version>.jar`

Dev client (Create + optional jars from `libs` / `devRuntimeMods`):

```bat
gradlew.bat runClient
```

Bare Create-only client (skip CBC / CBCMS / Create Nuclear jars):

```bat
gradlew.bat runClient -PbareClient
```

## License / 开源协议

This project is licensed under the [MIT License](LICENSE).

Third-party mods (Create, Create Nuclear, Create Big Cannons, CBC More Shells, etc.) remain under their respective licenses; this repository does not redistribute those jars.

## Credits / 致谢

- Create, Create Nuclear, Create Big Cannons, and CBC More Shells authors and communities  
- Contributors to Create Nuclear Armaments  

---

**Version:** 0.1.2 · **Loader:** NeoForge · **MC:** 1.21.1
