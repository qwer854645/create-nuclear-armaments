# Create Nuclear Armaments

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1-orange.svg)](https://neoforged.net/)

Weapons-grade nuclear munitions for the Create ecosystem — shells, bombs, rockets, torpedoes, and placeable charges — with mushroom-cloud FX, blast fallout, and optional Create Nuclear radiation hooks.

面向 Create 生态的核武器扩展：核炮弹 / 核炸弹 / 核火箭 / 核鱼雷 / 可放置核装置，含蘑菇云特效、冲击沉降，以及可选的 Create Nuclear 辐射兼容。

> **AI involvement / AI 参与说明**  
> Parts of this project were developed with assistance from AI coding tools (Cursor). Human authors review, test, and remain responsible for the released code.  
> 本项目部分内容由 AI 编程工具（Cursor）辅助完成；由人工审核、测试，并对发布代码负责。

## Features / 特性

- Tiered munitions **T1–T3** (shell, bomb, rocket, torpedo) and placeable nuclear charges  
  分级弹药 **T1–T3**（炮弹、炸弹、火箭、鱼雷）与可放置核装置
- Detonation FX: mushroom cloud, shock rim fracture, radiation / fallout terrain changes  
  起爆表现：蘑菇云、边缘碎裂、辐射沉降地形改造
- Chain ignition: TNT, CBC munition blocks, and placed charges can cascade  
  连锁点燃：TNT、CBC 弹药方块、放置核弹可连环引爆
- Soft dependencies: CBC / CBCMS / Create Nuclear content and recipes load only when those mods are present  
  软依赖：仅在 CBC / CBCMS / Create Nuclear 存在时注册对应内容并启用配方

## Dependencies / 依赖

| Mod | Required? | Notes |
|-----|-----------|--------|
| Minecraft 1.21.1 + NeoForge | **Yes** | — |
| [Create](https://modrinth.com/mod/create) 6.x | **Yes** | Registration via CreateRegistrate |
| [Create Nuclear](https://modrinth.com/mod/create-nuclear) | Optional | Materials + radiation effects |
| [Create Big Cannons](https://modrinth.com/mod/create-big-cannons) | Optional | Nuclear shells |
| [CBC More Shells](https://modrinth.com/mod/cbc-more-shells) (Military Supplement) | Optional | Nuclear bombs / rockets / torpedoes |

无 CBC / CBCMS 时不注册对应核弹药；无 Create Nuclear 时不注册核材料物品。

## Important notes / 注意事项

1. CBC-family munitions need a proper **fuze** to detonate as designed.  
   CBC 系弹药需正确安装**引信**。
2. Placeable charges: redstone uses a long fuse (~30s); TNT / blast chain uses a shorter fuse (~5s). Flint-and-steel will not ignite them.  
   放置核装置：红石长引信；TNT/爆炸连锁为短引信；打火石无效。
3. Bedrock-tier unbreakable blocks are not destroyed; the blast core can clear obsidian-tier blocks.  
   基岩等不可破坏方块保留；内核可清掉黑曜石级方块。
4. High-yield blasts are heavy on performance and may exhaust the sound pool.  
   大当量爆炸可能卡顿并打满音效池。
5. This is early **0.1.x** — expect balance and API changes. Prefer a test world.  
   早期 **0.1.x** 版本，数值与内容可能大幅调整，建议测试世界。

## Building / 构建

```bat
gradlew.bat build
```

Output jar: `build/libs/createnucleararmaments-<version>.jar`

Dev client (with libs + Create runtime mods configured in `build.gradle`):

```bat
gradlew.bat runClient
```

## License / 开源协议

This project is licensed under the [MIT License](LICENSE).

Third-party mods (Create, Create Nuclear, Create Big Cannons, CBC More Shells, etc.) remain under their respective licenses; this repository does not redistribute those jars.

## Credits / 致谢

- Create, Create Nuclear, Create Big Cannons, and CBC More Shells authors and communities  
- Contributors to Create Nuclear Armaments  

---

**Version:** 0.1.0 · **Loader:** NeoForge · **MC:** 1.21.1
