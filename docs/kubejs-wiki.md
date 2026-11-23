# Tetra JS KubeJS 使用指南

Tetra JS 把 Tetra 的模块化物品、工作台、原理图和 TetraWear 护甲系统接到 KubeJS 上。模块、图纸、材料、替换、改进仍然走数据包；脚本负责注册物品、监听运行时缝隙、查询状态，以及补数据包做不到的自定义逻辑。

本页面向整合包作者和脚本使用者。环境以 Minecraft 1.20.1、Forge、Tetra `>=6.17.0`、KubeJS 6、TetraWear `1.0.0+` 为准。

## 目录

- [它做什么 / 不做什么](#它做什么--不做什么)
- [环境与依赖](#环境与依赖)
- [脚本入口与绑定](#脚本入口与绑定)
- [注册模块化物品](#注册模块化物品)
- [事件](#事件)
- [查询与工具绑定](#查询与工具绑定)
- [工作台需求与合成效果](#工作台需求与合成效果)
- [无限 Improvement](#无限-improvement)
- [TetraWear](#tetrawear)
- [Curios](#curios)
- [统计条、排序与语言](#统计条排序与语言)
- [卷轴](#卷轴)
- [其它数据包补丁](#其它数据包补丁)
- [ProbeJS](#probejs)
- [常见写法](#常见写法)

## 它做什么 / 不做什么

Tetra JS 导出的是 **脚本会反复改、数据包又改不了** 的运行时缝隙，例如工作台合成结果、锤头消耗、射出箭矢、闪避、能量消耗。

不要指望它把 Tetra / TetraWear 内部的每个 Forge 事件再包一层。攻击、挖掘、受伤、HUD、按键、capability 挂载继续用 KubeJS `NativeEvents`。新模块、图纸、材料、替换、改进继续写数据包。

TetraWear 没有对外事件 API。脚本真正该碰的是 4 个运行时事件加一套查询绑定，见 [TetraWear](#tetrawear)。

## 环境与依赖

| 模组 | 关系 |
| --- | --- |
| Tetra `>=6.17.0` | 必须 |
| KubeJS | 必须 |
| TetraWear `>=1.0.0` | 必须 |
| Curios | 可选。加载后才会注册饰品物品类型和 `CuriosHelper` |
| ProbeJS Legacy | 可选。加载后会补 `ItemEffect` / `ItemAspect` 类型 |
| MoreRequirement | **不要再装**。功能已经并进 Tetra JS，同时存在会直接拒绝加载 |

脚本和数据包都放在 KubeJS 目录：

- `kubejs/startup_scripts`：注册物品、附魔 Aspect、自定义需求 / 合成效果
- `kubejs/server_scripts`：服务端事件、查询、消耗拦截
- `kubejs/client_scripts`：统计条、原理图列表、语言
- `kubejs/data`：Tetra 模块 / 图纸 / 材料，以及 Tetra JS 自己的 `group`、`scrolls`

## 脚本入口与绑定

全局事件组是 `TetraJSEvents`。下面这些名字在脚本里可以直接用：

| 绑定名 | 用途 |
| --- | --- |
| `TetraJSUtils` | 判断模块化物品、读写改进和 Aspect、生成卷轴 |
| `TetraDynamicModularHelper` / `DynamicModularHelper` | 给动态槽位物品读写 NBT 槽位 |
| `TetraShowModelHelper` / `ShowModelHelper` | 控制模块模型哪些 type 会渲染 |
| `TetraStatBarHelper` / `StatBarHelper` | 组装统计条用的 `IStatGetter` |
| `TetraLangUtils` | 补材料 / 图纸 / 效果的语言键 |
| `TetraSchematicUtils` | 读原理图经验、锤等级、类型、稀有度 |
| `TetraDataManager` | Tetra 的 `DataManager.instance` |
| `TetraSchematicRegistry` | Tetra 的 `SchematicRegistry` |
| `TetraItemModularHandheld` | Tetra 手持模块化物品类 |
| `TetraCustomRequirement` | 注册自定义图纸需求 |
| `TetraCustomCraftingEffectCondition` | 注册自定义合成效果条件 |
| `TetraCustomCraftingEffect` | 注册自定义合成效果结果 |
| `TetraWearHelper` | 查询能量、温度、护甲效果和属性 |
| `CuriosHelper` | 仅 Curios 加载时存在 |

字符串可以直接当成 `ItemEffect`、`ItemAspect`、`ToolAction` 用。动态槽位也可以直接丢 JS 对象，见 [动态槽位](#动态槽位物品)。

## 注册模块化物品

在 `startup_scripts` 里用 KubeJS 物品注册，第二个参数是 Tetra JS 的物品类型。槽位坐标是工作台 GUI 上的模块偏移，不是世界坐标。

```js
StartupEvents.registry('item', event => {
  event.create('pack:modular_blade', 'TetraJS:BaseModularItem')
    .tjs$addMajorSlot('blade', -4, 0)
    .tjs$addMajorSlot('hilt', 4, 18)
    .tjs$addMinorSlot('guard', 0, 8)
    .tjs$isCanHone(true)
    .tjs$setHoneBase(450)
    .tjs$setHoneIntegrityMultiplier(200)
    .tjs$setSynergiesPath('sword/', 'shared/')
    .maxStackSize(1)
})
```

所有模块化物品都继承普通 KubeJS `ItemBuilder`，所以 `.displayName()`、`.texture()`、`.tag()` 仍然可用。Tetra JS 自己的方法带 `tjs$` 前缀，避免和 KubeJS 撞名。

### 物品类型

| 类型 | 适用 | 槽位怎么来 |
| --- | --- | --- |
| `TetraJS:BaseModularItem` | 普通手持工具 / 武器 | 注册时写死主槽 / 副槽 |
| `TetraJS:TwoHandedModularItem` | 主手和副手都生效属性 | 同上 |
| `TetraJS:BowModularItem` | 模块化弓 | 同上，自带拉弓模型变体 |
| `TetraJS:CrossBowModularItem` | 模块化弩 | 同上，自带装填模型变体 |
| `TetraJS:ShieldModularItem` | 模块化盾 | 同上，自带盾牌模型和格挡 / 投掷覆盖 |
| `TetraJS:EquipModularItem` | 可装备到指定装备槽 | 同上，默认主手 |
| `TetraJS:DynamicModularItem` | 槽位写在物品 NBT 里 | 运行时用 `TetraDynamicModularHelper` |
| `TetraJS:CuriosModularItem` | Curios 饰品 | 注册时写死槽位 |
| `TetraJS:DynamicCuriosModularItem` | 动态槽位的 Curios 饰品 | NBT 槽位 + Curios 槽 |

没有 `TetraJS:ModularArmorItem`。给现有 TetraWear 护甲加模块、图纸、材料继续走数据包；不要用 `EquipModularItem` 冒充护甲。

### 共用方法

```js
.tjs$addMajorSlot(slotName, x, y)              // 主槽，默认 required
.tjs$addMajorSlot(slotName, x, y, required)
.tjs$addMinorSlot(slotName, x, y)              // 副槽，默认非 required
.tjs$addMinorSlot(slotName, x, y, required)
.tjs$isCanHone(boolean)                        // 默认 true
.tjs$setHoneBase(int)                          // 默认 450
.tjs$setHoneIntegrityMultiplier(int)           // 默认 200
.tjs$setSynergiesPath('sword/', 'shared/')     // 只加载 tetra 命名空间下、路径前缀匹配的 synergy
```

`tjs$setSynergiesPath` 过滤的是 `tetra` 命名空间里的 synergy 数据。路径写成 `'sword/'` 时，会吃到 `tetra:sword/xxx` 这类 synergy。

每种物品都会自动注册对应的修复原理图，key 是物品路径名。

### 可装备物品

```js
event.create('pack:modular_amulet', 'TetraJS:EquipModularItem')
  .tjs$addMajorSlot('core', 0, 0)
  .tjs$setSlot('HEAD')            // EquipmentSlot，默认 MAINHAND
  .tjs$setProviderAttribute(true) // 损坏时不提供属性
```

这是可装备的模块化物品，**不是** `ArmorItem`，也没有 TetraWear 的四件套槽位、trim、鞘翅飞行或护甲模型。头 / 胸 / 腿 / 靴请用 TetraWear 原版护甲物品。

### 动态槽位物品

动态物品注册时不写槽位。槽位存在物品 NBT `extraslots` 里，可以在获得物品、合成结束或命令里补：

```js
TetraDynamicModularHelper.addSlot(stack, 'blade', -4, 0, true)           // 主槽，required 跟随 isMajor
TetraDynamicModularHelper.addSlot(stack, 'guard', 0, 8, false, false)    // 副槽，非必须
TetraDynamicModularHelper.removeSlot(stack, 'guard')
TetraDynamicModularHelper.clearSlot(stack)
let slots = TetraDynamicModularHelper.getSlotData(stack)
```

也可以直接丢对象，字段是 `slot`、`x`、`y`，可选 `isMajor`、`required`，默认都是 `true`：

```js
TetraDynamicModularHelper.addSlot(stack, {
  slot: 'blade',
  x: -4,
  y: 0,
  isMajor: true,
  required: true
})
```

### 模型显示

模块模型默认会按 synergy + 全部模块渲染。物品 NBT `showModels` 可以只显示某些 model type：

```js
TetraShowModelHelper.addShowModel(stack, 'item', 'drawn')
TetraShowModelHelper.removeShowModel(stack, 'drawn')
TetraShowModelHelper.hasShowModel(stack, 'item')
TetraShowModelHelper.clearShowModel(stack)
```

列表为空时显示全部。弓 / 弩还会自动附加当前拉弓或装填变体。

## 事件

`TetraJSEvents` 按脚本侧分流。能取消的事件用 `event.cancel()`，或在回调里 `return false`。

| 事件 | 脚本侧 | 可取消 | 何时触发 |
| --- | --- | --- | --- |
| `registerEnchantAspect` | startup | 否 | 模组初始化，注册 Aspect 到附魔分类 |
| `registerStatBar` | client | 否 | 客户端注册 GUI 统计条 |
| `registerStatSorter` | client | 否 | 客户端注册统计排序器 |
| `workbenchTileCraft` | common | 否，但能替换产物 | 工作台真正合成时，客户端和服务器都会到 |
| `workbenchTileUpdateSchematicList` | client | 否，能改列表 | 工作台槽位展开原理图列表 |
| `hammerBlockCraftConsumeTool` | server | 是 | 巨型锤头方块消耗工具 |
| `createArrow` | server | 否，能换弹体 | 模块化弓 / 弩射出箭矢。额外参数 `'bow'` / `'crossbow'` |
| `dodge` | server | 是 | TetraWear 闪避，能量检查之后、位移之前 |
| `energyDrain` | server | 是 | TetraWear 能量真正扣除前 |
| `energyExhaust` | server | 否 | 能量从非耗尽变成耗尽 |
| `temperatureUpdate` | server | 否，能改目标和惩罚 | TetraWear 温度 tick，大约每秒一次 |
| `stealthVisibility` | server | 否，能改可见度 | 潜行可见度算完 base × guise 之后 |

### 附魔 Aspect

```js
TetraJSEvents.registerEnchantAspect(event => {
  event.register('edged_weapon', 'WEAPON', 'BREAKABLE')
})
```

第一个参数是 `ItemAspect`（字符串即可），后面是原版 `EnchantmentCategory`。Tetra 会生成 `additions/<aspect>` 和 `exclusions/<aspect>` 规则。

### 工作台合成

```js
TetraJSEvents.workbenchTileCraft(event => {
  let schematic = event.currentSchematic
  let slot = event.currentSlot
  let before = event.targetStack
  let after = event.upgradedStack

  if (event.player.isCreative()) {
    event.setUpgradedStack(after)
  }
})
```

字段：`targetStack`、`upgradedStack`、`player`、`level`、`workbenchTile`、`materials`、`materialsAltered`、`currentSchematic`、`currentSlot`。

调用 `setUpgradedStack` 后，Tetra JS 会用你给的物品覆盖产物、清材料槽、清当前图纸，并跳过后续原版收尾。不调用则走 Tetra 自己的合成。

### 原理图列表

客户端事件。`event.schematicList` 是可变数组，可以删、可以加：

```js
TetraJSEvents.workbenchTileUpdateSchematicList(event => {
  event.schematicList.removeIf(schematic =>
    TetraSchematicUtils.isHoning(schematic) && !event.player.isCreative()
  )
})
```

无效图纸（`getRarity()` / `getType()` / `getKey()` 为空或抛错）会在排序前被丢掉，避免工作台 GUI 崩。

### 锤头消耗

```js
TetraJSEvents.hammerBlockCraftConsumeTool(event => {
  if (event.player.isCreative()) {
    event.cancel()
  }
})
```

取消后返回原 `targetStack`，不消耗。只读字段：`level`、`pos`、`baseBlockState`、`hammerHeadBlock`、`targetStack`、`player`、`requiredTool`、`requiredLevel`、`consumeResources`。

### 射出箭矢

必须带额外 ID，区分弓和弩：

```js
TetraJSEvents.createArrow('bow', event => {
  let arrow = event.projectile
  arrow.setBaseDamage(arrow.getBaseDamage() + event.drawProgress * 0.1)
  event.setProjectile(arrow)
})

TetraJSEvents.createArrow('crossbow', event => {
  // 弩没有 drawProgress
})
```

字段：`item`、`level`、`ammoItem`、`ammoStack`、`projectile`、`player`、`drawProgress`（仅弓）。`setProjectile` 之后才会替换实际射出的实体。

弓和弩的事件是分开的，监听 `'bow'` 不会收到弩。

TetraWear 相关事件见 [TetraWear](#tetrawear)。

## 查询与工具绑定

### TetraJSUtils

```js
TetraJSUtils.isModularItem(item)
TetraJSUtils.stackIsTetraMaterial(stack)

let level = TetraJSUtils.getImprovementLevel(stack, 'blade', 'enchantment/sharpness') // 没有则 -1
TetraJSUtils.addImprovement(stack, 'blade', 'enchantment/sharpness', 3)
TetraJSUtils.addCheckedImprovement(stack, 'blade', 'enchantment/sharpness', 3) // 只有更高等级才写入

TetraJSUtils.getAspectLevel(stack, 'blade', 'edged_weapon')
TetraJSUtils.hasAspectLevel(stack, 'blade', 'edged_weapon')
TetraJSUtils.getAspects(stack, 'blade', 'edged_weapon')
```

`addCheckedImprovement` 成功返回 `true`。Aspect 可以直接传字符串。`getAspects` 的第三个参数目前不会过滤结果，但 Java 签名需要传入一个 Aspect。

生成卷轴物品：

```js
let scroll = TetraJSUtils.setupScrollData(
  'pack/nether_blade',
  'details.key',
  ['sword/blade', 'sword/guard'],
  ['hone/nether'],
  true,          // intricate
  0,             // material
  0x8B0000,      // tint
  [0, 1, 2, 3]   // glyphs
)
```

原理图和合成效果路径会自动加上 `tetra:` 命名空间。整合包自己的图纸如果不是 `tetra` 命名空间，不要走这个方法，改用数据包 `data/tetrajs/scrolls`。

### TetraSchematicUtils

```js
TetraSchematicUtils.getExperienceCost(schematic, target, materials, slot)
TetraSchematicUtils.getRequiredHammerLevel(schematic, target, materials)
TetraSchematicUtils.getRequiredToolLevel(schematic, target, materials, 'hammer_dig')
TetraSchematicUtils.getMaxRequiredToolLevel(schematic, target, materials)
TetraSchematicUtils.getTotalRequiredToolLevel(schematic, target, materials)
TetraSchematicUtils.getType(schematic)      // other / improvement / minor / major
TetraSchematicUtils.getRarity(schematic)    // hone / temporary / basic
TetraSchematicUtils.willReplaced(schematic, target, materials, slot)
TetraSchematicUtils.isHoning(schematic)
TetraSchematicUtils.isConfigSchematic(schematic)
TetraSchematicUtils.isRepairSchematic(schematic)
TetraSchematicUtils.isEnchantSchematic(schematic)
```

## 工作台需求与合成效果

Tetra JS 内置了一套图纸需求（`CraftingRequirement`）和合成效果条件（`CraftingEffectCondition`）。写在 Tetra 原理图的 `requirement` 或合成效果的 `condition` 里，类型 ID 以 `tetrajs:` 开头。

这些需求会显示在全息图上。语言键在 `tetrajs.holo.*`。

### 需求类型

| type | 字段 | 含义 |
| --- | --- | --- |
| `tetrajs:advancement` | `advancement` | 玩家已完成该进度 |
| `tetrajs:all_improvement` | `improvement`, `level` | 工具任意模块拥有该改进。`level <= 0` 表示任意等级 |
| `tetrajs:biome` | `biome` | 工作台所在群系 |
| `tetrajs:dimension` | `dimension` | 所在维度 |
| `tetrajs:enchantment` | `enchantment`, `level` | 物品拥有指定附魔，等级至少为 `level`，默认 1 |
| `tetrajs:height` | `min`, `max` | 工作台 Y 坐标，默认 `-99` ~ `333` |
| `tetrajs:see_sky` | 无 | 工作台上方直到世界顶都没有挡运动的方块 |
| `tetrajs:time` | `time` | `day` 或 `night` |
| `tetrajs:weather` | `weather` | `clear` / `rain` / `thunder` |
| `tetrajs:moon_phase` | `moonPhase` | `1` 满月 … `8` 盈凸月 |
| `tetrajs:potion` | `effect`, `duration`, `amplifier` | 玩家药水。时长单位是 tick，等级是 amplifier |
| `tetrajs:entities` | `entities`, `x`, `y`, `z` | 工作台周围实体数量。默认范围 5 / 2 / 5 |
| `tetrajs:mbd` | `blocks`, `x`, `y`, `z` | 工作台周围方块数量。默认范围 3 / 2 / 3 |
| `tetrajs:other_module` | `slot`, `requirement` | 把内部需求改去检查另一个槽 |
| `tetrajs:group` | `key`, `hideExtend` | 引用 `data/tetrajs/group` 里的需求组 |
| `tetrajs:custom` | `key` | 交给脚本判断 |

`tetrajs:other_module` 和 `tetrajs:group` 只用于图纸需求，不是合成效果条件。

合成效果额外有：

| type | 字段 | 含义 |
| --- | --- | --- |
| `tetrajs:wrap_target` | `requirement` | 把条件改去检查工作台目标物品，而不是合成后的物品 |
| `tetrajs:custom` | `key` | 脚本合成效果结果，见下面 |

维度 + 露天示例：

```json
{
  "type": "tetrajs:dimension",
  "dimension": "minecraft:the_nether"
}
```

多模块结构用 `tetrajs:mbd`：

```json
{
  "type": "tetrajs:mbd",
  "x": 3,
  "y": 2,
  "z": 3,
  "blocks": {
    "minecraft:obsidian": 8,
    "minecraft:nether_bricks": 4
  }
}
```

周围实体：

```json
{
  "type": "tetrajs:entities",
  "x": 6,
  "y": 2,
  "z": 6,
  "entities": {
    "minecraft:blaze": 1
  }
}
```

月相从 1 开始：满月、亏凸月、下弦月、残月、新月、蛾眉月、上弦月、盈凸月。

药水里的 `duration` 是 tick，`amplifier` 是原版放大器（0 表示 I 级）。

### 需求组

把一组需求抽出来复用。文件放在 `kubejs/data/tetrajs/group/<name>.json`，进游戏后会同步给客户端。

```json
{
  "key": "nether_forge",
  "translation": "tetrajs.group.nether_forge",
  "hideExtend": false,
  "requirements": [
    { "type": "tetrajs:dimension", "dimension": "minecraft:the_nether" },
    { "type": "tetrajs:height", "min": 32, "max": 120 }
  ]
}
```

`key` 缺省时用文件路径。`translation` 缺省时是 `tetrajs.group.<key>`。图纸里引用：

```json
{
  "type": "tetrajs:group",
  "key": "nether_forge"
}
```

`hideExtend` 为 true 时，全息图只显示组名，不展开子需求。

### 自定义需求与合成效果

先在数据包里写 `key`，再在脚本里注册同名函数。建议放 `startup_scripts`，保证数据包加载前就已经登记。

```js
TetraCustomRequirement.register('near_boss', cxt => {
  return cxt.player != null && cxt.world != null && cxt.pos != null
})

TetraCustomCraftingEffectCondition.register('near_boss', cxt => {
  return cxt.player().isAlive()
})

TetraCustomCraftingEffect.register('give_ember', ctx => {
  if (ctx.consumeResources()) {
    ctx.player().giveExperiencePoints(20)
  }
})
```

图纸：

```json
{ "type": "tetrajs:custom", "key": "near_boss" }
```

合成效果结果：

```json
{
  "outcomes": [
    { "type": "tetrajs:custom", "key": "give_ember" }
  ]
}
```

自定义需求的全息文本是 `tetrajs.holo.custom_requirement.<key>`，需要自己补语言。

`CraftingContext` 是 Tetra 原版对象，常见字段是 `player`、`world`、`pos`、`targetStack`、`slot`。合成效果条件的上下文是 record，用方法读：`player()`、`upgradedStack()`、`materials()`、`schematic()` 等。

## 无限 Improvement

模块改进 JSON 可以加 `"infinite": true`。一份定义可以对应任意已存储等级，属性和效果按存储等级缩放。

```json
{
  "key": "pack/tempering",
  "level": 1,
  "infinite": true,
  "effects": {
    "stabilizing": 1
  }
}
```

行为：

- 物品上已有该改进时，再添加会把等级累加，而不是覆盖成固定 level
- 属性、模块属性和 ItemEffect 都按当前存储等级倍增
- 材料改进（`MaterialImprovementData`）会把 `infinite` 带到合成结果
- Tetra 的 `StackMode` 对无限改进不再二次堆叠，避免和累加打架

适合“每次锻造 +1，没有上限”的成长，不适合原版那种 1 / 2 / 3 档离散改进。

## TetraWear

TetraWear 现在是硬依赖。闪避、能量、温度、潜行都有事件；日常查询走 `TetraWearHelper`。模块、图纸、材料、替换、改进仍然走数据包。

不要为 `charge` / `evade` / `inertia` / `shadowstep` / `skitter` / `retreatingShot` 各写一个事件，它们都从同一次闪避分发。也不要为 `lifesteal`、`witheringArmor`、`harvestSpeed` 这类效果各写事件，它们只是读护甲 ItemEffect 后做事。新效果继续用 Tetra 数据驱动的 ItemEffect。

没有护甲 honing API，也没有 `magmaWalker`。`data/tetrawear/stealth_effects` 目前没有被 TetraWear 消耗，不要把它当稳定接口。客户端 overlay、护甲模型、trim / 鞘翅渲染、config 热更新也不导出。

### 查询

```js
let player = event.player

TetraWearHelper.hasModularArmor(player)
TetraWearHelper.isModularArmorItem(item)
TetraWearHelper.getArmorEffects(player)

TetraWearHelper.getCurrent(player)
TetraWearHelper.getMax(player)
TetraWearHelper.getRegen(player)
TetraWearHelper.isExhausted(player)
TetraWearHelper.canUseEnergy(player)
TetraWearHelper.isEffectivelyEnabled(player)
TetraWearHelper.drain(player, 8) // reason 会变成 script

TetraWearHelper.getCurrentTemperature(player)
TetraWearHelper.getTargetTemperature(player)
TetraWearHelper.getTempZone(player)
TetraWearHelper.getPenalty(player)           // cold / freezing / hot / searing / null
TetraWearHelper.getHeatResistance(player)
TetraWearHelper.getColdResistance(player)

TetraWearHelper.isDodgeEnabled(player)
TetraWearHelper.canDodge(player)
```

`getArmorEffects` 合并四件模块化护甲的 `EffectData`。`drain` 会走 `energyDrain` 事件，reason 是 `script`。

### 属性

直接拿 `Attribute`，方便读玩家属性或注册统计条：

```js
player.getAttributeValue(TetraWearHelper.agility())
player.getAttributeValue(TetraWearHelper.stealth())
player.getAttributeValue(TetraWearHelper.dodgeStrength())
player.getAttributeValue(TetraWearHelper.dodgeEnergyCost())
player.getAttributeValue(TetraWearHelper.heatResistance())
player.getAttributeValue(TetraWearHelper.coldResistance())
player.getAttributeValue(TetraWearHelper.energyAttackDamage())
player.getAttributeValue(TetraWearHelper.energyAttackCost())
player.getAttributeValue(TetraWearHelper.energyHarvestSpeed())
player.getAttributeValue(TetraWearHelper.energyHarvestCost())
player.getAttributeValue(TetraWearHelper.energyArmor())
player.getAttributeValue(TetraWearHelper.energyToughness())
player.getAttributeValue(TetraWearHelper.energyDefenceCost())
player.getAttributeValue(TetraWearHelper.energyItemUseCost())
```

### 效果常量

```js
TetraWearHelper.elytra
TetraWearHelper.dazzling
TetraWearHelper.snowWalker
TetraWearHelper.insulating
TetraWearHelper.waterBreathing
TetraWearHelper.charge
TetraWearHelper.evade
TetraWearHelper.inertia
TetraWearHelper.shadowstep
TetraWearHelper.skitter
TetraWearHelper.retreatingShot
TetraWearHelper.lifesteal
TetraWearHelper.webbingArmor
TetraWearHelper.witheringArmor
TetraWearHelper.harvestSpeed
TetraWearHelper.dampenFall
TetraWearHelper.webWalker
TetraWearHelper.skeletalSway
TetraWearHelper.shadowSway
```

没有 `magmaWalker`。闪避衍生效果仍然由 TetraWear 自己处理，脚本只要拦总闪避事件。

### 闪避

服务端，可取消。发生在能量足够之后、真正位移之前。取消则不位移、不消耗、不触发衍生效果。

```js
TetraJSEvents.dodge(event => {
  if (event.player.isCreative()) {
    event.energyCost = 0
    event.saturationCost = 0
    return
  }
  if (event.direction == 'BACK') {
    event.cancel()
  }
  event.strength = event.strength * 1.25
})
```

| 字段 | 可写 | 说明 |
| --- | --- | --- |
| `player` | 否 | 玩家 |
| `direction` | 否 | `FRONT` / `BACK` / `LEFT` / `RIGHT` |
| `strength` | 是 | 属性 `dodge_strength` × 配置 |
| `energyCost` | 是 | 属性 `dodge_energy_cost` × 配置。`<= 0` 时跳过扣能量 |
| `saturationCost` | 是 | 饥饿消耗 |

原版闪避衍生效果继续由 TetraWear 处理，不要拆成六个事件。

### 能量消耗 / 耗尽

能量是整套护甲的共享资源。攻击、落空、挖掘、防御、拉弓、持续使用、举盾、闪避最后都进 `drain()`。

```js
TetraJSEvents.energyDrain(event => {
  if (event.player.isCreative()) {
    event.cancel()
    return
  }
  if (event.reason == 'mine') {
    event.amount = event.amount * 0.5
  }
})

TetraJSEvents.energyExhaust(event => {
  event.player.tell(`能量耗尽：${event.reason}`)
})
```

`energyDrain` 可取消，可改 `amount`。`exhausted` 是消耗前的状态。`energyExhaust` 不可取消，适合做惩罚、状态、任务。

当前能区分的 `reason`：

| reason | 来源 |
| --- | --- |
| `attack` | 能量攻击加成 |
| `miss` | 挥空 |
| `mine` | 能量挖掘加成 |
| `defend` | 能量防御加成 |
| `dodge` | 闪避 |
| `use` | `EnergyHelper.drain`，包括使用物品、弓、盾、持续消耗等没单独挂钩的路径 |
| `script` | `TetraWearHelper.drain` |
| `unknown` | 没有上下文 |

不要给 `EnergyAttackEffect` / `EnergyMiningEffect` / `EnergyDefensiveEffect` 再各做一套事件。那些已经有属性和 `tetrawear-server.toml` 的 `ON / OFF / REQUIRES_ATTRIBUTE`。

### 温度

大约每秒一次，发生在算出 target 之后、回血 / 把温度交给能量系统之前。

```js
TetraJSEvents.temperatureUpdate(event => {
  if (event.player.level.dimension.toString() == 'minecraft:the_nether') {
    event.setTarget(Math.max(event.target, 80))
  }
  if (event.player.isCreative()) {
    event.clearPenalty()
  }
})
```

| 字段 | 可写 | 说明 |
| --- | --- | --- |
| `current` | 否 | 当前温度 |
| `target` | 是 | 目标温度 |
| `biomeTemperature` | 否 | 群系基础温度 × 100 |
| `timeMultiplier` | 否 | 一天内的温度倍率 |
| `zone` | 否 | 温度带 |
| `heatResistance` / `coldResistance` | 否 | 抗性 |
| `penalty` | 是 | `cold` / `freezing` / `hot` / `searing` / `null` |

`setPenalty` / `clearPenalty` 会覆盖 TetraWear 按 zone 和抗性算出的惩罚。无效名字会被当成没有惩罚。群系 / 维度 / 高度该有多热，数据包改不了，要接其他温度模组或关回血就用这个事件。

### 潜行可见度

基础潜行已经走 Forge `LivingVisibilityEvent`，KubeJS `NativeEvents` 能听。Tetra JS 补的是 guise 判定写死之后、`modifyVisibility` 之前那一刀。

```js
TetraJSEvents.stealthVisibility(event => {
  if (event.lookingEntity && event.lookingEntity.type == 'minecraft:warden') {
    event.setVisibility(1.0)
    return
  }
  if (event.matchedGuises.contains('netherStealth')) {
    event.addGuiseModifier(0.5)
  }
})
```

| 字段 | 可写 | 说明 |
| --- | --- | --- |
| `sneaker` | 否 | 潜行者 |
| `lookingEntity` | 否 | 正在看的实体 |
| `baseModifier` | 否 | 属性 + 潜行 + 光照 / 面向 |
| `guiseModifier` | 是 | 伪装倍率。改它会重算 `visibility`，除非已经手动 `setVisibility` |
| `visibility` | 是 | 最终可见度乘数 |
| `matchedGuises` | 否 | 已匹配的伪装 |

当前能识别的 guise：

- `undeadStealth`：观察者是亡灵
- `arthropodStealth`：观察者是节肢
- `netherStealth`：潜行者位于下界群系 tag

`addGuiseModifier(0.5)` 是再乘一次。`setVisibility` 之后再改 `guiseModifier` 不会覆盖你设的最终值。

### 护甲物品和标签

TetraWear 四件套：

- `tetrawear:modular_helmet`
- `tetrawear:modular_chest`
- `tetrawear:modular_leggings`
- `tetrawear:modular_boots`

头盔槽：lining / cover / attachment。胸甲槽：inner / outer / arms。给这些物品加内容只写数据包，不需要新 builder。

下面这些标签可以直接在数据包里用，Tetra JS 没有再包一层 API：

- `tetrawear:energy_bow_include` / `energy_bow_exclude`
- `tetrawear:energy_instant_use_include` / `energy_instant_use_exclude`
- `tetrawear:energy_continuous_use_include` / `energy_continuous_use_exclude`
- `tetrawear:energy_shield_include` / `energy_shield_exclude`
- `tetrawear:bypasses_energy_armor`
- `tetrawear:web_walkable`

## Curios

只有加载了 Curios 才会出现对应物品类型和 `CuriosHelper`。没装时不要在脚本里碰这些名字。

```js
StartupEvents.registry('item', event => {
  event.create('pack:modular_ring', 'TetraJS:CuriosModularItem')
    .tjs$addCuriosSlot('ring')
    .tjs$addMajorSlot('gem', 0, 0)
    .tjs$addMinorSlot('band', 0, 12)
    .tjs$setProviderAttribute(true)
    .tjs$isUnbreakable()
})
```

`tjs$addCuriosSlot('ring')` 实际是打上 `curios:ring` 物品标签，可多次调用。`tjs$isUnbreakable()` 后不会损坏、不会被判定为破损。属性只通过 Curios 槽生效，不会在原版装备槽再算一遍。

查询身上所有模块化饰品：

```js
CuriosHelper.getAllModularItemsInCurios(player)
CuriosHelper.hasCuriosEffectLevel(player, 'stabilizing')
CuriosHelper.getCuriosEffectLevel(player, 'stabilizing')      // 求和
CuriosHelper.getCuriosEffectMaxLevel(player, 'stabilizing')   // 取最大
CuriosHelper.getCuriosEffectEfficiency(player, 'stabilizing')
CuriosHelper.getCuriosEffectMaxEfficiency(player, 'stabilizing')
```

动态饰品用 `TetraJS:DynamicCuriosModularItem`，槽位仍然用 `TetraDynamicModularHelper`。

## 统计条、排序与语言

统计条和排序器只在客户端注册。

```js
TetraJSEvents.registerStatBar(event => {
  event.registerEffectBar('stabilizing')
    .setMax(20)
    .setAddEfficiencyTooltip(true)
    .setLabelGetter('integerLabel')

  event.registerAttributeBar(TetraWearHelper.agility())
    .setMax(40)
    .setIgnoreBase(true)
    .setAttributeGetterType('COMMON')
    .addOneDecimalTooltip(TetraStatBarHelper.attribute(TetraWearHelper.agility()))
})
```

效果条默认用效果等级做 getter，tooltip 默认整数。属性条默认一位小数。`setAttributeGetterType` 可以是 `COMMON`、`ADDITION`、`MULTIPLY`。

常用 builder 方法：

- `setMin` / `setMax` / `setSegmented` / `setSplit` / `setInverted`
- `setLabelGetter('percentageLabel')`
- `addIntegerTooltip` / `addOneDecimalTooltip` / `addTowDecimalTooltip` / `addAbbreviateTooltip`
- `addExtendGetter` / `addExtendedGetter`、`setTooltipGetter`、`setShowCheck`、`addStatIndicator`

语言键默认是 `tetra.stats.<effect或attribute>`，tooltip 是这个键加 `.tooltip`；扩展 tooltip 继续使用同一键加 `_extended`。`addExtendedGetter` 的值会按扩展语言键的 `%s` 顺序传入。也可以用 `TetraStatBarHelper.newTooltipGetter('tetra.stats.example.tooltip')` 明确构造并通过 `setTooltipGetter` 使用扩展 getter。

指示器复用 Tetra 6.17.0 的 `GuiStatIndicator` API：

```js
let tooltip = TetraStatBarHelper.newTooltipGetter('tetra.stats.stabilizing.tooltip')
  .addIntegerTooltip(TetraStatBarHelper.effectLevel('stabilizing'))
  .addExtendedGetter(TetraStatBarHelper.effectEfficiency('stabilizing'))
  .build()
let indicator = TetraStatBarHelper.newStatIndicator(
  0, 0, 'tetra.stats.stabilizing', 0,
  TetraStatBarHelper.effectLevel('stabilizing'), tooltip
).withShowRequirements(TetraStatBarHelper.effectLevel('stabilizing'))

TetraJSEvents.registerStatBar(event => {
  event.registerEffectBar('stabilizing')
    .addStatIndicator(indicator)
})
```

默认构造器使用 Tetra glyphs 纹理；需要自定义纹理时使用带 `width`、`height`、`ResourceLocation`/资源路径的 `newStatIndicator` 重载。
自己拼 getter：

```js
let getter = TetraStatBarHelper.sum(
  TetraStatBarHelper.effectLevel('stabilizing'),
  TetraStatBarHelper.effectEfficiency('stabilizing', 0.5)
)
```

还有 `effectLevel`、`effectEfficiency`、`attribute`、`attributeAddition`、`attributeMultiply`、`integrity`、`magicCapacity`、`multiply`、`showWrapper`、`newTooltipGetter`。

排序器：

```js
TetraJSEvents.registerStatSorter(event => {
  event.registerBasicSorter(
    TetraStatBarHelper.effectLevel('stabilizing'),
    'Stabilizing',
    'oneDecimal'
  )
})
```

格式：`noDecimal`、`oneDecimal`、`twoDecimal`、`abbreviate`。

数据包也可以注册只对某些物品生效的 getter，类型是 `tetrajs:item`：

```json
{
  "type": "tetrajs:item",
  "stat": { "type": "tetra:effect_level", "effect": "stabilizing" },
  "items": ["tetrawear:modular_chest"],
  "tag": "forge:armors/chestplates"
}
```

`items` 和 `tag` 满足任一即可。

补语言：

```js
ClientEvents.lang('zh_cn', event => {
  let json = TetraLangUtils.getLackMaterialLang()
  json.keySet().forEach(key => event.add(key, json.get(key).getAsString()))

  TetraLangUtils.addSchematic(json, 'sword/nether_blade', '下界刀刃', '在下界锻造的刀刃', '刀刃')
  TetraLangUtils.addEffect(json, 'stabilizing', '稳定', '降低抖动', '短说明', '长说明')
})
```

`getLackMaterialLang` 会给还没有 `tetra.material.<key>` 的材料补上对应物品名。`addSchematic` / `addEffect` 是往同一个 `JsonObject` 里写键，写完后同样要 `event.add` 进去。

可用的 label：`integerLabel`、`integerLabelInverted`、`decimalLabel`、`singleDecimalLabel`、`decimalLabelInverted`、`percentageLabel`、`percentageLabelInverted`、`percentageLabelDecimal`、`percentageLabelDecimalInverted`。

## 卷轴

除了 `TetraJSUtils.setupScrollData`，还可以在 `kubejs/data/tetrajs/scrolls/` 放 JSON 数组。数据会同步，并出现在创造标签页。

```json
[
  {
    "key": "pack/nether_blade",
    "details": "pack.nether_blade.details",
    "intricate": true,
    "material": 0,
    "ribbon": "8B0000",
    "glyphs": [0, 1, 2, 3],
    "schematics": ["tetra:sword/blade"],
    "effects": ["tetra:hone/nether"]
  }
]
```

配置项 `CreateSingleCreativeTab` 默认 `true`，会单独建一个卷轴创造页。设为 `false` 时，卷轴进 Tetra 默认创造页。

空的 ribbon 颜色不会再让卷轴读取崩溃。

## 其它数据包补丁

这些不是事件，但写数据包时会碰到：

- 模块模型的 `transformation` 除了 `rotation`，还可以写 `rotationL` 和 `rotationR`，分别作为左右四元数。
- Tetra 原版模块化弓、弩、盾补了缺失的修复原理图。
- 工作台预览和合成时，替换同一模块不会再把该槽附魔全部清掉；只有新模块不接受的附魔才会被移除。
- 同一模块上重复附魔会取较高等级，而不是直接崩。

## ProbeJS

装了 ProbeJS Legacy 后，Tetra JS 会把当前游戏里所有 `ItemEffect` 和 `ItemAspect` 的 key 写成字面量联合类型，并提供 `@ItemEffect`、`@ItemAspect` 补全片段。事件和 `TetraWearHelper` 走普通 Java 绑定，ProbeJS 可以直接看到方法。

## 常见写法

创造模式取消闪避消耗，但不取消闪避本身：

```js
TetraJSEvents.dodge(event => {
  if (event.player.isCreative()) {
    event.energyCost = 0
    event.saturationCost = 0
  }
})
```

任务期间禁止能量消耗：

```js
TetraJSEvents.energyDrain(event => {
  if (event.player.persistentData.getBoolean('packNoEnergy')) {
    event.cancel()
  }
})
```

刚耗尽时给效果：

```js
TetraJSEvents.energyExhaust(event => {
  event.player.potionEffects.add('minecraft:slowness', 60, 0)
})
```

下界把温度目标抬高，创造模式清惩罚：

```js
TetraJSEvents.temperatureUpdate(event => {
  if (event.player.level.dimension.toString() == 'minecraft:the_nether') {
    event.setTarget(event.target + 20)
  }
  if (event.player.isCreative()) event.clearPenalty()
})
```

对监守者取消伪装：

```js
TetraJSEvents.stealthVisibility(event => {
  if (event.lookingEntity && event.lookingEntity.type == 'minecraft:warden') {
    event.setVisibility(event.baseModifier)
  }
})
```

读状态而不改事件：

```js
PlayerEvents.tick(event => {
  if (event.level.client) return
  let player = event.player
  if (!TetraWearHelper.hasModularArmor(player)) return
  player.persistentData.putDouble('twEnergy', TetraWearHelper.getCurrent(player))
  player.persistentData.putBoolean('twExhausted', TetraWearHelper.isExhausted(player))
  player.persistentData.putInt('twZone', TetraWearHelper.getTempZone(player))
})
```

## 不要做的事

- 不要再装 MoreRequirement。
- 不要用 `EquipModularItem` 做 TetraWear 护甲。现有护甲改数据包；新护甲物品类型目前没有。
- 不要把 TetraWear 内部的受伤、挖掘速度、攻击、tick、HUD、按键再包一层，KubeJS `NativeEvents` 已经能听 Forge 事件。
- 不要为每个闪避衍生效果或每个能量加成效果单独做事件。
- 不要把 `data/tetrawear/stealth_effects` 当 API。
- 不要导出或依赖未实现的 `magmaWalker`。
- 模块、图纸、材料、替换、改进继续走数据包。脚本只处理运行时缝隙和查询。
