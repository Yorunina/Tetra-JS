# TetraWear KubeJS 导出方案

本文是 Tetra JS 对接 TetraWear 的后续开发方案，不改代码，只定范围和落地顺序。

分析对象：`build.gradle` 中的 `curse.maven:tetrawear-670545:8570793`（TetraWear `1.0.0`，要求 Tetra `>=6.17.0`）。TetraWear 没有公开源码仓库，结论来自该 jar 的反编译结果。

## 结论

TetraWear **没有对外事件 API**。它几乎全是内部 Forge 订阅、mixin 和数据包内容。本模组不该把每个 `@SubscribeEvent` 都转成 KubeJS 事件。

真正该给脚本的是 **4 个运行时缝隙 + 一套查询绑定**。模块、图纸、材料、替换、改进继续走数据包。

这和本模组现有事件风格一致：只导出「数据包到不了、脚本又会反复改」的缝，例如 workbench craft、createArrow、hammer consume。

## 不该做的事

- 把 TetraWear 内部的 `LivingHurtEvent`、`BreakSpeed`、`AttackEntityEvent`、tick、HUD、按键、capability 挂载再包一层。KubeJS NativeEvents 已经能听 Forge 事件；TetraWear 也没往这些事件里塞独特 payload。
- 为 `charge` / `evade` / `inertia` / `shadowstep` / `skitter` / `retreatingShot` 各做一套事件。它们都从 `Dodge.dodgePlayer()` 分发，一个 dodge 事件就够。
- 为 `lifesteal` / `witheringArmor` / `webbingArmor` / `dampenFall` / `skeletalSway` / `harvestSpeed` 各做事件。它们只是「读护甲 ItemEffect → 做一件事」。新效果应走 Tetra 数据驱动 ItemEffect，或本模组已有的自定义 effect 路径。
- 导出护甲 honing。受伤 / 击杀 / 挖方块涨 hone 已有 config：`honeWearKillEntityAmount`、`honeWearBreakBlockChance`。
- 把 `magmaWalker` 当 API。`ArmorItemEffect` 里有常量，源码里没有实现。
- 把 `data/tetrawear/stealth_effects` DataStore 当稳定接口。数据通道在，`StealthSystem` 还没消费。
- 导出客户端 overlay、护甲模型、trim / 染色渲染、config 热更新。

## 该导出的事件

### 1. Dodge（最高优先）

`se.mickelus.tetrawear.abilities.dodge.Dodge.dodgePlayer()` 是 TetraWear 唯一的玩家主动动作。闪避成功后硬编码分发给：

- `charge`（仅向前）
- `evade`
- `inertia`
- `shadowstep`
- `skitter`
- `retreatingShot`（仅向后）

建议：`TetraJSEvents.dodge`，server，`.hasResult()`。

脚本可见信息：

- player
- direction（front / back / left / right）
- strength（属性 `dodge_strength` × config）
- energyCost（属性 `dodge_energy_cost` × config）
- saturationCost
- 是否能量足够 / 是否系统启用

脚本应能：

- 取消闪避
- 改力度、能量消耗、饱和度消耗
- 自己挂额外效果

原版 dodge 衍生效果继续由 TetraWear 自己跑，不要拆成六个事件。

挂钩点：`Dodge.dodgePlayer()` 入口，能量检查之后、实际位移之前；取消则不位移、不消耗、不触发衍生效果。

### 2. EnergyDrain / EnergyExhaust（第二优先）

能量是整套护甲系统的共享资源。攻击、落空、挖掘、受伤、拉弓、持续使用、举盾、闪避最后都进 `PlayerEnergy.drain()`。耗尽会进入 `exhausted`，并关掉能量加成、降低攻击和挖掘。

建议两个事件：

**`TetraJSEvents.energyDrain`**，server，`.hasResult()`

- player
- amount
- reason：`attack` / `miss` / `mine` / `defend` / `dodge` / `bow` / `use` / `shield` / `continuous`
- current / max
- exhausted（消耗前）

脚本应能改数量或取消消耗。

**`TetraJSEvents.energyExhaust`**，server，不必可取消

- player
- current / max
- 触发这次 exhausted 的 reason / amount

适合做惩罚、状态、任务。不要把 `EnergyAttackEffect`、`EnergyMiningEffect`、`EnergyDefensiveEffect` 再各做一套事件。那些已经有属性和 `tetrawear-server.toml` 的 `ON / OFF / REQUIRES_ATTRIBUTE`。单次消耗卡在 `drain()` 即可。

挂钩点：`PlayerEnergy.drain(double)` 以及 exhausted 从 false 变为 true 的瞬间。reason 需要从各调用点传入，或在调用栈可区分处包一层。

### 3. TemperatureUpdate（中优先）

温度有 capability，但目标温度是 Java 硬编码：群系温度 × 昼夜倍率，再映射成 zone，用 `heat_res` / `cold_res` 判定 `hot` / `searing` / `cold` / `freezing`。舒服区还会额外回血。

数据包改不了「这个群系 / 维度 / 高度该有多热」。整合包若要接其它温度模组、自定义维度，或关掉回血，只能靠事件。

建议：`TetraJSEvents.temperatureUpdate`，server，约每秒一次（现有 tick 就是 `gameTime % 20 == 0`）。

脚本可见信息：

- player
- current / target
- biome 温度、时间倍率
- zone
- heatResistance / coldResistance
- penalty（`cold` / `freezing` / `hot` / `searing` / null）

脚本应能改 target，或改 / 清空 penalty。

不必导出 capability 挂载和 HUD。

挂钩点：`PlayerTemperature.tick()` 算出 target 之后、应用回血 / 把温度交给能量系统之前。

### 4. StealthVisibility / Guise（中优先）

基础潜行已经走 Forge `LivingVisibilityEvent`，KubeJS NativeEvents 能听。真正缺的是 guise 判定写死了：

- `undeadStealth` → `MobType.UNDEAD`
- `arthropodStealth` → `MobType.ARTHROPOD`
- `netherStealth` → 下界群系 tag

同时 `TetraWearData` 已经有 `data/tetrawear/stealth_effects` 的 DataStore，结构是 `(ItemEffect, EntityTypePredicate, exclude, amount)`，但 `StealthSystem` 没有消费它。这是半成品数据通道。

只做一套，不要两套都做。优先事件：

**`TetraJSEvents.stealthVisibility`**，server

- sneaker（玩家）
- lookingEntity
- baseModifier（属性 + 潜行 + 光照 / 面向）
- guiseModifier
- 已匹配的 guise 效果
- 最终 visibility 乘数

脚本应能改最终乘数，或追加 / 覆盖 guise。

备选方案：绑定让脚本注册新 guise 条件，接到现有 DataStore。事件更符合本模组习惯。

挂钩点：`StealthSystem.onLivingVisibility()` 算出 base × guise 之后、`event.modifyVisibility()` 之前。

## 该导出的绑定

脚本日常更多是查状态，不是拦事件。对照现有 `TetraJSUtils` / `CuriosHelper`，增加 `TetraWearHelper`，仅在 TetraWear loaded 时注册。

能量：

- `get(player)` / `getCurrent` / `getMax` / `getRegen` / `isExhausted`
- `canUseEnergy(player)`
- `isEffectivelyEnabled(player)`
- `drain(player, amount)`

温度：

- `getCurrent` / `getTarget` / `getZone`
- `getPenalty`
- `getHeatResistance` / `getColdResistance`

护甲：

- `hasModularArmor(player)`
- `getArmorEffects(living)`（四件模块化护甲的 `EffectData` 合并）
- 物品：`tetrawear:modular_helmet` / `modular_chest` / `modular_leggings` / `modular_boots`

属性（直接暴露 RegistryObject / Attribute，方便脚本读玩家属性）：

- `agility`
- `stealth`
- `dodge_strength` / `dodge_energy_cost`
- `heat_res` / `cold_res`
- `energy_attack_damage` / `energy_attack_cost`
- `energy_harvest_speed` / `energy_harvest_cost`
- `energy_armor` / `energy_toughness` / `energy_defence_cost`
- `energy_item_use_cost`

效果常量：

- 护甲：`elytra`、`dazzling`、`snowWalker`、`insulating`、`waterBreathing`
- 闪避衍生：`charge`、`evade`、`inertia`、`shadowstep`、`skitter`、`retreatingShot`
- 其它：`lifesteal`、`webbingArmor`、`witheringArmor`、`harvestSpeed`、`dampenFall`、`webWalker`、`skeletalSway`、`shadowSway`

不要导出未实现的 `magmaWalker`。

标签（只作查询说明，不必包一层 API）：

- `tetrawear:energy_bow_include` / `energy_bow_exclude`
- `tetrawear:energy_instant_use_include` / `energy_instant_use_exclude`
- `tetrawear:energy_continuous_use_include` / `energy_continuous_use_exclude`
- `tetrawear:energy_shield_include` / `energy_shield_exclude`
- `tetrawear:bypasses_energy_armor`
- `tetrawear:web_walkable`

## 物品构建器是另一件事

现有 `TetraJS:EquipModularItem` 只是可装备的模块化物品，不是 `ArmorItem`，也没有 TetraWear 的：

- 四件套槽位（helmet: lining / cover / attachment，chest: inner / outer / arms）
- trim / 染色 replacement hook
- elytra 飞行
- VoidArmorModel 以及 humanoid / skin / elytra / trim 模型类型
- 每件护甲基础 agility +5

如果目标只是给 **现有 tetrawear 护甲** 加模块、图纸、材料，不需要新事件，也不需要新 builder，数据包即可。

如果目标是注册 **新的模块化护甲物品**，再做类似 Curios 的 `TetraJS:ModularArmorItem` builder。这是注册面，不是本方案第一期。

## 和现有代码的对接方式

- 事件挂到 `TetraJSEvents`，仅在 `ModList.get().isLoaded("tetrawear")` 时注册。
- 绑定走 `CompatManager.registerCompatBindings`，与 Curios 同一套可选兼容入口。
- 挂钩用 mixin 打在 TetraWear 的 `Dodge`、`PlayerEnergy`、`PlayerTemperature`、`StealthSystem` 上，不要改 TetraWear 的数据包内容。
- 不要为能量的 7 个 effect 类分别 mixin。reason 能在 `drain()` 调用点区分就在调用点记；区分不了就先做无 reason 的 drain 事件，第二期再补。
- ProbeJS：给新事件和 `TetraWearHelper` 补类型，沿用 `TetraProbePlugin`。

## 落地顺序

1. Compat 探测 + `TetraWearHelper` 绑定：查能量、温度、护甲效果、属性、效果常量。
2. `dodge` 事件。
3. `energyDrain` / `energyExhaust`。
4. 有整合包温度 / 潜行需求时再做 `temperatureUpdate` 和 `stealthVisibility`。
5. 只有确认要自定义护甲物品时，再做 ModularArmor builder。

## TetraWear 内部对照（开发时用）

| 系统 | 关键类 | 现有挂钩 | 脚本价值 |
| --- | --- | --- | --- |
| 闪避 | `abilities.dodge.Dodge` | 无公共事件，包 `DodgePacket` | 高，唯一主动动作 |
| 能量 | `systems.energy.PlayerEnergy` / `EnergySystem` | 无公共事件，内部 drain | 高，共享资源 |
| 温度 | `systems.temperature.PlayerTemperature` | capability + 硬编码公式 | 中，算法改不了 |
| 潜行 | `systems.stealth.StealthSystem` | Forge `LivingVisibilityEvent` + 写死 guise | 中，guise 扩展 |
| 护甲物品 | `item.ModularArmor` 及四件套 | 数据包模块 / 图纸 | 低，除非新物品 |
| 能量加成 | `EnergyAttack/Mining/DefensiveEffect` | 属性 + config mode | 低，不必再包事件 |
| 护甲 honing | `effects.ArmorHoning` | config | 低 |
| 数据 | `data.TetraWearData.stealthEffectData` | DataStore 未消费 | 暂不作为 API |

## 验收标准

第一期完成后，脚本应能在不改数据包的前提下做到：

- 读取玩家当前能量、是否疲惫、温度 zone、是否穿着模块化护甲。
- 取消某次闪避，或把某次闪避的能量消耗改成 0。
- 拦截某次能量消耗（例如创造模式、某任务期间）。
- 在玩家刚疲惫时给效果或发消息。

第二期才要求：改温度目标、改 guise 对特定实体的可见度乘数。
