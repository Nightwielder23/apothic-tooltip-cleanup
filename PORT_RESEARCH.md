# Apothic Tooltip Cleanup - 1.21.1 NeoForge Port Research

Research compiled 2026-05-21 for porting Apothic Tooltip Cleanup from Minecraft 1.20.1
Forge to 1.21.1 NeoForge.

## Sources used

This research did **not** use a local CurseForge test instance. No 1.21.1 NeoForge
instance exists on this machine. Every CurseForge instance under
`C:\Users\ayand\curseforge\minecraft\Instances\` is Minecraft 1.20.1 Forge (or one 1.19.2
instance), and the only Apotheosis jars present anywhere on disk are `Apotheosis-1.20.1-7.4.8`
and `Apotheosis-1.19.2-6.5.2`. See the note at the end of this document.

Instead, the following authoritative artifacts were fetched and inspected directly:

- **`Apotheosis-1.21.1-8.5.3.jar`** - downloaded from the official maven
  `https://maven.shadowsoffire.dev/releases/dev/shadowsoffire/Apotheosis/1.21.1-8.5.3/`.
  Class signatures were read with `javap` (JDK 21). Lang and TOML files were read directly
  from the jar. Decompiled findings below cite this jar by class name.
- **`Placebo-1.21.1-9.9.1.jar`** - downloaded from the same maven. Used for
  `DynamicHolder` / `DynamicRegistry`.
- **`neoforge-21.1.228-sources.jar`** - real NeoForge source, taken from the Gradle cache.
  This is the exact NeoForge version the 1.21.1 MDK targets (`neo_version=21.1.228`).
- **`loader-4.0.42.jar`** (FancyModLoader) - taken from the Gradle cache. Used for the
  `@Mod`, `@EventBusSubscriber`, `ModList`, `ModContainer` definitions.

Public source for Apotheosis lives at `https://github.com/Shadows-of-Fire/Apotheosis`
(the 1.21 line of development). The class and package names below were verified against the
shipped 8.5.3 jar, which is the binding source of truth for this port.

---

## 1. Apotheosis 8.x data component for affix and rarity data

**Status: confirmed.**

The 1.20.1 NBT path `stack.getTag().getCompound("affix_data").getString("rarity")` is
**completely gone**. There is no `affix_data` compound. In 8.x every piece of affix/rarity
data is its own `DataComponentType`, registered in the inner class
`dev.shadowsoffire.apotheosis.Apoth$Components` (decompiled from `Apotheosis-1.21.1-8.5.3.jar`):

```
public static final DataComponentType<ItemAffixes> AFFIXES;
public static final DataComponentType<DynamicHolder<LootRarity>> RARITY;
public static final DataComponentType<Component> AFFIX_NAME;
public static final DataComponentType<Integer> SOCKETS;
public static final DataComponentType<ItemContainerContents> SOCKETED_GEMS;
public static final DataComponentType<DynamicHolder<Gem>> GEM;
public static final DataComponentType<Purity> PURITY;
public static final DataComponentType<Float> DURABILITY_BONUS;
public static final DataComponentType<Boolean> FROM_CHEST;
public static final DataComponentType<Boolean> FROM_TRADER;
public static final DataComponentType<Boolean> FROM_BOSS;
public static final DataComponentType<Boolean> FROM_MOB;
public static final DataComponentType<Boolean> CHARM_ENABLED;
public static final DataComponentType<Block>   STONEFORMING_TARGET;
public static final DataComponentType<Boolean> MALICE_MARKER;
public static final DataComponentType<Boolean> TOUCHED_BY_MALICE;
```

### Registry IDs

The component IDs were read from the `bootstrap()` bytecode of `Apoth$Components`. They are
registered through `Apoth.R` (a `dev.shadowsoffire.placebo.registry.DeferredHelper`) whose
namespace is `apotheosis`. The string literals passed to `DeferredHelper.component(...)` are:

| Field          | Namespaced ID                | Java value type                       |
|----------------|------------------------------|---------------------------------------|
| `AFFIXES`      | `apotheosis:affixes`         | `ItemAffixes`                         |
| `RARITY`       | `apotheosis:rarity`          | `DynamicHolder<LootRarity>`           |
| `AFFIX_NAME`   | `apotheosis:affix_name`      | `net.minecraft.network.chat.Component`|
| `SOCKETS`      | `apotheosis:sockets`         | `Integer` (codec range 0..16)         |
| `SOCKETED_GEMS`| `apotheosis:socketed_gems`   | `ItemContainerContents` (vanilla type)|
| `GEM`          | `apotheosis:gem`             | `DynamicHolder<Gem>`                  |
| `PURITY`       | `apotheosis:purity`          | `Purity` (enum)                       |
| `DURABILITY_BONUS` | `apotheosis:durability_bonus` | `Float`                          |

Note the affix-name component is `apotheosis:affix_name`, not the old NBT `name` string, and
sockets are split into a plain `apotheosis:sockets` integer count plus an
`apotheosis:socketed_gems` `ItemContainerContents` holding the gem item stacks.

### How to extract rarity

Do **not** read the component map directly. Use the public helper
`dev.shadowsoffire.apotheosis.affix.AffixHelper`:

```java
public static DynamicHolder<LootRarity> getRarity(ItemStack stack);
```

Its decompiled body is literally:

```
stack.getOrDefault(Apoth.Components.RARITY, RarityRegistry.INSTANCE.emptyHolder())
```

So it always returns a non-null `DynamicHolder`. If the item has no rarity the holder is the
registry's empty holder and `isBound()` returns `false`.

`AffixInstance` is a `record` and **does not** carry the rarity id as a string. Its shape is:

```java
public record AffixInstance(
        DynamicHolder<Affix> affix,
        float level,
        DynamicHolder<LootRarity> rarity,
        ItemStack stack) { ... }
```

To turn rarity into a string for switch-matching (see section 2):

```java
DynamicHolder<LootRarity> holder = AffixHelper.getRarity(stack);
if (holder.isBound()) {
    ResourceLocation id = holder.getId();   // e.g. apotheosis:mythic
    String path = id.getPath();             // "mythic"
    LootRarity rarity = holder.get();       // the record itself
    TextColor color = rarity.color();       // the actual rarity color
}
```

`DynamicHolder` also exposes `getRegistryPath()` which returns the path string directly.

The original example given in the task ("returns an AffixInstance record, rarity accessed via
`instance.rarity().getId()`") is close but slightly off for 8.5.3: the per-item rarity is its
own component (`apotheosis:rarity`), and `AffixInstance.rarity()` returns
`DynamicHolder<LootRarity>` (not a value with a direct `getId()` until you call
`DynamicHolder.getId()`).

---

## 2. Apotheosis 8.x rarity registry

**Status: confirmed.**

`LootRarity` is **not** a vanilla `Registry` and not a datapack `Registries` registry. It is a
Placebo "dynamic registry" - a JSON reload listener. The registry class is
`dev.shadowsoffire.apotheosis.loot.RarityRegistry`:

```java
public class RarityRegistry extends TieredDynamicRegistry<LootRarity> {
    public static final RarityRegistry INSTANCE;
    public static DynamicHolder<LootRarity> getMaterialRarity(Item material);
    public static List<LootRarity> getSortedRarities();
}
```

`TieredDynamicRegistry<LootRarity>` extends `dev.shadowsoffire.placebo.reload.DynamicRegistry<LootRarity>`.
Its constructor bytecode passes the path string `"rarities"`, so rarities are loaded from
`data/<namespace>/rarities/*.json`.

### Lookup API (inherited from `DynamicRegistry`)

```java
LootRarity                  getValue(ResourceLocation id);
ResourceLocation            getKey(LootRarity value);
LootRarity                  getOrDefault(ResourceLocation id, LootRarity fallback);
Set<ResourceLocation>       getKeys();
Collection<LootRarity>      getValues();
DynamicHolder<LootRarity>   holder(ResourceLocation id);
DynamicHolder<LootRarity>   holder(LootRarity value);
DynamicHolder<LootRarity>   emptyHolder();
```

So a `ResourceLocation` to `LootRarity` lookup is `RarityRegistry.INSTANCE.getValue(rl)` or
`RarityRegistry.INSTANCE.holder(rl)`.

### Getting the rarity id as a string

`LootRarity` is a record with **no** id field:

```java
public record LootRarity(
        TextColor color,
        Holder<Item> material,
        TieredWeights weights,
        List<LootRule> rules,
        int sortIndex,
        RarityRenderData renderData,
        SoundEvent invaderSound) { ... }
```

The id only exists as the registry key. Get it via `RarityRegistry.INSTANCE.getKey(rarity)`
(returns `ResourceLocation`) or, if you already have the `DynamicHolder`, via
`holder.getId().getPath()`.

### Rarity additions / renames since 1.20.1

**This is an important change.** The built-in Apotheosis datapack in 8.5.3 ships **only five**
rarity files:

```
data/apotheosis/rarities/common.json
data/apotheosis/rarities/uncommon.json
data/apotheosis/rarities/rare.json
data/apotheosis/rarities/epic.json
data/apotheosis/rarities/mythic.json
```

There is **no `ancient.json`**. In 1.20.1 `ancient` was a sixth built-in rarity; in 8.5.3 it
is gone from the default datapack. The translation key `rarity.apotheosis:ancient` still
exists in the lang file (line 716 of `en_us.json`) for backwards compatibility and for
datapacks/add-ons that re-add it, but vanilla Apotheosis no longer registers it.

Consequence for the port: the existing `RarityColorOverride` switch
(`common/uncommon/rare/epic/mythic/ancient`) is still safe, but `ancient` now only ever
matches if a datapack or add-on adds an `ancient` rarity. The `default -> ANCIENT` fall-through
in `resolveRarityHex` becomes a generic "unknown rarity" fallback rather than a real tier.

Rarity colors (from `data/apotheosis/rarities/*.json`): common `#808080`, mythic `#ED7014`.
The `color` is a real field on the `LootRarity` record (`TextColor color()`), so the rarity
color override feature can read the true color directly instead of hardcoding hex values
(see section 9).

---

## 3. Apotheosis 8.x translation keys

**Status: confirmed.**

Verified against `assets/apotheosis/lang/en_us.json` inside `Apotheosis-1.21.1-8.5.3.jar`.

| Key the 1.20.1 code matches        | State in 8.5.3                                            |
|------------------------------------|-----------------------------------------------------------|
| `text.apotheosis.dot_prefix`       | **Exists.** `"text.apotheosis.dot_prefix": "• %s"`   |
| `text.apotheosis.fits_in`          | **Gone / renamed.** Replaced by `text.apotheosis.socketable_into`. |
| `text.apotheosis.socketable_into`  | **Exists.** `"text.apotheosis.socketable_into": "Fits In:"`|
| `text.apotheosis.when_socketed_in` | **Exists.** `"text.apotheosis.when_socketed_in": "When Socketed in:"` |
| `text.apotheosis.unique`           | **Exists.** `"text.apotheosis.unique": "Unique"`          |

The current `TooltipMatcher` / `FitsInRemover` code matches both `socketable_into` and
`fits_in`. Since `fits_in` no longer exists, the `fits_in` branch is harmless dead code; it can
be removed, but it does not need to be.

New `when_socketed` variants exist alongside the old one:

```
"text.apotheosis.when_socketed":       "When Socketed:"
"text.apotheosis.when_socketed_in":    "When Socketed in:"
"text.apotheosis.when_socketed_typed": "When Socketed in %s:"
```

`FitsInRemover` matches with `keyStartsWith(..., "text.apotheosis.when_socketed_in")`. Note
that `when_socketed_in` is also a prefix of nothing else, but `when_socketed` (no suffix) is a
prefix of all three. If the cleanup logic should also catch the plain and typed variants,
match `text.apotheosis.when_socketed` instead.

### New tooltip-related keys not matched by the original code

| Key                                       | Value / meaning                                  | Cleanup relevance |
|-------------------------------------------|--------------------------------------------------|-------------------|
| `text.apotheosis.world_tier.haven` ... `.pinnacle` | The five World Tier names (Haven, Frontier, Ascent, Summit, Pinnacle) and their `.desc` lines. World Tier is a player-progression system, shown mostly in GUIs. | Low - GUI text, not item tooltips. |
| `text.apotheosis.world_tier_tutorial`     | `"This item has an unknown power"` - shown on **unidentified** affix items. | Medium - this is a real item tooltip line a cleanup mod might want to leave alone or restyle. |
| `text.apotheosis.world_tier_tutorial.2`   | `"Press %s and activate World Tier: Haven to unlock it"` - second line of the above. | Medium - same. |
| `purity.apotheosis.cracked` ... `.perfect`| The six gem purity names. Purity is a new gem property (Cracked, Chipped, Flawed, Normal, Flawless, Perfect). Rendered on gem item names/tooltips. | Medium - a gem-tooltip cleanup mode may want to compact or hide purity text. |
| `text.apotheosis.purities`                | `"Purities"` - GUI label. | Low. |
| `text.apotheosis.socket_limit`            | `"Max Sockets Applied: %s"` - shown on items that have hit the socket cap. | Medium - a socket cleanup mode may want to hide this. |
| `text.apotheosis.facets`                  | `"Facets: %s"` - new gem stat line. | Medium. |
| `text.apotheosis.star_prefix`             | `"🌟 %s"` - a star bullet, sibling format to `dot_prefix`, used for emphasized lines. | Medium - same family as `dot_prefix`; matchers that key off `dot_prefix` will miss star-prefixed lines. |
| `text.apotheosis.equipped`                | `"Equipped"` - label drawn by the equipment-compare feature. | Low - only appears in the compare overlay. |
| `key.apotheosis.compare_equipment`        | `"Compare Hovered Equipment"` - keybind name. | None - keybind, not a tooltip. |
| `key.apotheosis.link_item_to_chat`        | `"Link Hovered Item to Chat"` - keybind name. | None - keybind. |
| `chat.apotheosis.link_item_with_count`    | `"%s %s"` - chat message format for item linking. | None - chat, not a tooltip. |
| `affix.apotheosis.cooldown`               | `"[⌛ %s]"` - cooldown annotation appended to affix lines. | Medium - new affix line decoration. |
| `affix.apotheosis.stacking`               | `"[Stacking]"` - stacking annotation on affix lines. | Medium - new affix line decoration. |
| `misc.apotheosis.blacklisted_potion`      | `"Cannot be converted into a Potion Charm."` | Low. |

Conclusions on the task's candidate list:

- **`world_tier`** - exists as GUI text plus the unidentified-item tutorial lines. A cleanup
  mod would generally leave these alone; the tutorial lines are the only ones on actual item
  tooltips.
- **`purity`** - exists as a new gem mechanic. Worth an optional toggle in the gem tooltip
  modes if gem tooltips are being compacted.
- **`augmentation`** - there is no `augmentation` tooltip key. The closest keys are
  `text.apotheosis.monster_augments` / `player_augments` (World Tier GUI labels) and the
  `block.apotheosis.augmenting_table` family. None are item tooltip lines.
- **`item_link`** - only keybind and chat keys exist (`key.apotheosis.link_item_to_chat`,
  `chat.apotheosis.link_item_with_count`, `message.apotheosis.item_linking_disabled`). Not a
  tooltip line; nothing to clean.
- **`equipment_compare`** - exists only as the keybind `key.apotheosis.compare_equipment` and
  the `text.apotheosis.equipped` overlay label. The compare feature renders a second tooltip
  next to the hovered one; it is not a set of lines inside one tooltip.

---

## 4. Apotheosis 8.x socket structure

**Status: confirmed (rendering path and keys); partially confirmed (Sigil cap behavior).**

### Translation keys

- Empty socket line: `socket.apotheosis.empty` -> `"Empty Socket"` (unchanged from 1.20.1).
- Filled gem "Socketed" affix line: `affix.apotheosis:socket.desc` -> `"Socketed (%s)"`.
- The filled-gem visual rows are built in code by
  `SocketTooltipRenderer.getSocketDesc(GemInstance)`, not by a single static key.

### How sockets render

Socket rendering still goes through the **`RenderTooltipEvent.GatherComponents` event**, not
through a data-component `TooltipProvider`. The full pipeline in 8.5.3
(class `dev.shadowsoffire.apotheosis.client.AdventureModuleClient`):

1. `affixTooltips(ItemTooltipEvent)` adds affix text lines and inserts a literal marker line
   where the socket block should appear.
2. `comps(RenderTooltipEvent$GatherComponents)` scans the tooltip elements, finds the marker,
   and inserts a `SocketComponent` (`TooltipComponent`) at that position. Decompiled:

   ```
   new SocketTooltipRenderer$SocketComponent(stack, SocketHelper.getGems(stack));
   ... Either.right(socketComponent);
   ... list.add(index, either);   // List<Either<FormattedText, TooltipComponent>>
   ```

3. `tooltipComps(RegisterClientTooltipComponentFactoriesEvent)` registers
   `SocketTooltipRenderer` as the `ClientTooltipComponent` factory for `SocketComponent`.

**The marker string changed.** In 1.20.1 it was `APOTH_REMOVE_MARKER`. In 8.5.3 the field
`AdventureModuleClient.GEM_SOCKET_MARKER` is built from the literal **`APOTH_SOCKET_MARKER`**.
`MarkerCleaner` (which currently searches for `"APOTH_REMOVE_MARKER"`) must be updated to
`"APOTH_SOCKET_MARKER"`. There is also a second marker, `StoneformingAffix.TOOLTIP_MARKER`,
handled the same way.

### Class / package moves (critical for `SocketCompactor`)

The `adventure` sub-package was removed in 8.x. The socket renderer moved:

- 1.20.1: `dev.shadowsoffire.apotheosis.adventure.client.SocketTooltipRenderer$SocketComponent`
- 8.5.3:  `dev.shadowsoffire.apotheosis.client.SocketTooltipRenderer$SocketComponent`

`SocketCompactor.SOCKET_COMPONENT_FQN` must be updated to the new path.

`SocketComponent` is now a record:

```java
public record SocketComponent(ItemStack socketed, SocketedGems gems)
        implements TooltipComponent { ... }
```

Reflection impact on the current `SocketCompactor`:

- `tc.getClass().getMethod("gems")` -> returns `SocketedGems` (was a plain `List` in 1.20.1).
- `tc.getClass().getMethod("socketed")` -> returns `ItemStack`. OK.
- `tc.getClass().getConstructor(ItemStack.class, gems.getClass())` -> matches
  `SocketComponent(ItemStack, SocketedGems)`. OK.
- `gems.getClass().getConstructor(List.class)` -> matches `SocketedGems(List<GemInstance>)`. OK.

`SocketedGems` is a record that **implements `List<GemInstance>`**:

```java
public record SocketedGems(ImmutableList<GemInstance> gems) implements List<GemInstance> { ... }
public static final SocketedGems EMPTY;
```

Because it is backed by an `ImmutableList`, **it can never contain `null`**. Empty sockets are
represented by an invalid `GemInstance` (`GemInstance.EMPTY`), not by `null`. The current
`SocketCompactor` loop checks both `inst == null` and `!isValid()`; the `null` branch is now
dead but harmless, and the `isValid()` branch still correctly counts empty sockets.
`GemInstance` exposes `public boolean isValid()`.

### Max socket count / Sigil of Socketing

- The `apotheosis:sockets` data component uses `Codec.intRange(0, 16)`, so 16 is the hard
  data ceiling for the stored socket count.
- Built-in rarity rules generate up to 4 sockets (see `data/apotheosis/rarities/mythic.json`,
  which has an `apotheosis:socket` rule with `min:4 max:4` or `min:1 max:3`).
- The line `text.apotheosis.socket_limit` -> `"Max Sockets Applied: %s"` is new in 8.x and is
  shown when an item has reached its socket cap.
- `item.apotheosis.sigil_of_socketing.desc` -> `"Adds one socket to an item"`.
- The specific "3-socket cap introduced in 8.3.0" claim **could not be confirmed** from the
  jar. The current `socket_limit` line format is confirmed, and `SocketHelper.getSockets`
  returns a plain `int`. Confirming the exact cap value and the 8.3.0 changelog behavior would
  require reading the Apotheosis changelog or `AddSocketsRecipe` logic in detail; the cap
  itself appears to be data/recipe driven rather than a fixed constant.

### Socket helper API

```java
public class SocketHelper {
    public static int          getSockets(ItemStack stack);
    public static SocketedGems getGems(ItemStack stack);
    public static boolean      hasEmptySockets(ItemStack stack);
    public static int          getFirstEmptySocket(ItemStack stack);
    public static boolean      canSocketGemInItem(ItemStack item, ItemStack gem);
}
```

---

## 5. NeoForge 1.21.1 ItemTooltipEvent

**Status: confirmed.**

Verified against `net/neoforged/neoforge/event/entity/player/ItemTooltipEvent.java` in
`neoforge-21.1.228-sources.jar` (the stable release the MDK targets).

- **Import:** `net.neoforged.neoforge.event.entity.player.ItemTooltipEvent`
- **Superclass:** `PlayerEvent` (`net.neoforged.neoforge.event.entity.player.PlayerEvent`).
- **Constructor:**

  ```java
  public ItemTooltipEvent(ItemStack itemStack,
                          @Nullable Player player,
                          List<Component> list,
                          TooltipFlag flags,
                          TooltipContext context)
  ```

  Parameter order: `ItemStack`, `@Nullable Player`, `List<Component>`, `TooltipFlag`,
  `Item.TooltipContext`.

- **New parameter:** yes, `TooltipContext context` where `TooltipContext` is
  `net.minecraft.world.item.Item.TooltipContext`. It is exposed via `getContext()`.
  `Item.TooltipContext` carries the registry access (`HolderLookup.Provider`) and level/time
  info that items need to resolve data-component-driven tooltips. **A tooltip cleanup mod that
  only filters, reorders, and recolors existing `Component` lines does not need it.** You would
  only touch it if you start resolving Apotheosis data components yourself in a context that
  needs registry access (which `AffixHelper` / `SocketHelper` already handle internally).

- **Getters (unchanged from Forge 1.20.1, still present):**
  `getItemStack()`, `getToolTip()` (the mutable `List<Component>`), `getFlags()`,
  `getEntity()` (the nullable player), plus the new `getContext()`.

- **Cancellation / priority:** `ItemTooltipEvent` does **not** implement `ICancellableEvent`,
  so it is not cancelable - same as Forge 1.20.1. Handlers subscribe with
  `@net.neoforged.bus.api.SubscribeEvent` and may set
  `priority = net.neoforged.bus.api.EventPriority.LOWEST` exactly as before. The
  `EventPriority` enum and `SubscribeEvent` annotation moved to the `net.neoforged.bus.api`
  package but behave identically. The current `TooltipHandler` (`@SubscribeEvent(priority =
  EventPriority.LOWEST)`) ports across with only import changes.

- **`RenderTooltipEvent.GatherComponents`** (used by `SocketCompactor`) is still
  `net.neoforged.neoforge.client.event.RenderTooltipEvent` with the inner class
  `GatherComponents`. `getTooltipElements()` still returns
  `List<Either<FormattedText, TooltipComponent>>`.

The migration for reading the event is purely import changes; the constructor change does not
affect a consumer that only reads the event.

---

## 6. NeoForge 1.21.1 mod loader patterns

**Status: confirmed.**

Verified against `loader-4.0.42.jar` (FancyModLoader) and the 1.21.1 MDK example sources
(`ExampleMod.java`, `ExampleModClient.java`).

### `@Mod` and the constructor

`@Mod` is `net.neoforged.fml.common.Mod`:

```java
public @interface Mod {
    String value();
    Dist[] dist() default { Dist.CLIENT, Dist.DEDICATED_SERVER };
}
```

The mod constructor is no longer parameterless. FML injects recognized parameter types. The
MDK uses:

```java
@Mod(ExampleMod.MODID)
public ExampleMod(IEventBus modEventBus, ModContainer modContainer) { ... }
```

Recognized injectable types include `IEventBus` (`net.neoforged.bus.api.IEventBus`, the mod
event bus), `ModContainer` (`net.neoforged.fml.ModContainer`), `Dist`, and
`FMLModContainer`. You may declare any subset in any order. For a client-only mod you can also
use `@Mod(value = MODID, dist = Dist.CLIENT)` (the MDK's `ExampleModClient` does this).

There is no more `FMLJavaModLoadingContext.get().getModEventBus()` - take the `IEventBus`
as a constructor parameter instead. `NeoForge.EVENT_BUS` (`net.neoforged.neoforge.common.NeoForge`)
is still the game event bus for manual registration.

### Replacement for `@Mod.EventBusSubscriber`

It is now a **top-level annotation**, `net.neoforged.fml.common.EventBusSubscriber` (no longer
nested inside `@Mod`):

```java
public @interface EventBusSubscriber {
    Dist[] value() default { Dist.CLIENT, Dist.DEDICATED_SERVER };
    String modid() default "";
    Bus bus() default Bus.GAME;
}
```

Example from the MDK:

```java
@EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
```

### Replacement for `Mod.EventBusSubscriber.Bus.FORGE` / `.MOD`

The enum is `net.neoforged.fml.common.EventBusSubscriber.Bus` with **two** constants:

| 1.20.1 Forge                          | 1.21.1 NeoForge                         | Meaning                                  |
|---------------------------------------|-----------------------------------------|------------------------------------------|
| `Mod.EventBusSubscriber.Bus.FORGE`    | `EventBusSubscriber.Bus.GAME`           | The game/runtime event bus (`NeoForge.EVENT_BUS`) - gameplay events such as `ItemTooltipEvent`, `RenderTooltipEvent`. |
| `Mod.EventBusSubscriber.Bus.MOD`      | `EventBusSubscriber.Bus.MOD`            | The mod lifecycle/registration event bus (the `IEventBus` passed to the constructor). |

`FORGE` was renamed to `GAME`. `MOD` kept its name. `Bus.GAME` is the default if `bus` is
omitted.

For the port: `TooltipHandler` and `SocketCompactor` currently use
`bus = Mod.EventBusSubscriber.Bus.FORGE` -> change to `EventBusSubscriber.Bus.GAME` (or just
drop the `bus` argument, since `GAME` is the default), and change the annotation import to
`net.neoforged.fml.common.EventBusSubscriber`.

### Registering a client-only config

`ModLoadingContext.get().registerConfig(...)` is gone. Register through the `ModContainer`
that FML injects into the constructor:

```java
public ApothicTooltipCleanup(ModContainer modContainer) {
    modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
}
```

`ModContainer.registerConfig` (from `ModContainer.class` in `loader-4.0.42.jar`):

```java
public void registerConfig(ModConfig.Type type, IConfigSpec spec);
public void registerConfig(ModConfig.Type type, IConfigSpec spec, String fileName);
```

`ModConfig.Type` is still `net.neoforged.fml.config.ModConfig.Type` with
`CLIENT`, `COMMON`, `SERVER`, `STARTUP`. A `ModConfigSpec` is an `IConfigSpec`, so it is passed
directly.

Optional but recommended for a config-heavy mod: NeoForge can auto-generate an in-game config
screen. Register it in the (client) constructor:

```java
modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
```

(`net.neoforged.neoforge.client.gui.IConfigScreenFactory` /
`net.neoforged.neoforge.client.gui.ConfigurationScreen`.) The MDK's `ExampleModClient` does
exactly this.

---

## 7. NeoForge 1.21.1 ModConfigSpec

**Status: confirmed.**

Verified against `net/neoforged/neoforge/common/ModConfigSpec.java` in
`neoforge-21.1.228-sources.jar`.

- **Rename:** yes. `ForgeConfigSpec` is now `ModConfigSpec`.
  - Import: `net.neoforged.neoforge.common.ModConfigSpec`.
  - Nested types likewise rename: `ModConfigSpec.Builder`, `ModConfigSpec.ConfigValue<T>`,
    `ModConfigSpec.BooleanValue`, `ModConfigSpec.IntValue`, `ModConfigSpec.DoubleValue`,
    `ModConfigSpec.LongValue`, `ModConfigSpec.EnumValue<T>`.

- **Builder API:** mostly identical. `comment(...)`, `push(...)`, `pop(...)`,
  `define(...)`, `defineInList(...)`, `defineInRange(...)`, `build()` are all unchanged in
  signature and behavior. `defineInList` and `defineInRange` both still exist with the same
  shapes the current `Config.java` uses.

- **`defineListAllowEmpty` changed.** The 3-argument form the current code uses still
  compiles but is now `@Deprecated`:

  ```java
  // Deprecated - kept for compatibility, no "add element" button in the config UI:
  @Deprecated
  public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(
          String path, List<? extends T> defaultValue, Predicate<Object> elementValidator);

  // Preferred form - adds a Supplier<T> for new elements created via the config screen:
  public <T> ConfigValue<List<? extends T>> defineListAllowEmpty(
          String path, List<? extends T> defaultValue,
          Supplier<T> newElementSupplier, Predicate<Object> elementValidator);
  ```

  The current `Config.HIDDEN_AFFIX_IDS` line
  (`defineListAllowEmpty("hidden_affix_ids", Collections.emptyList(), o -> o instanceof String)`)
  will still compile against NeoForge 21.1.228, only emitting a deprecation warning. To clear
  the warning and enable the in-UI add button, switch to the 4-arg form, e.g.:
  `defineListAllowEmpty("hidden_affix_ids", List.of(), () -> "", o -> o instanceof String)`.
  (The MDK's `Config.java` uses exactly this 4-arg pattern.)

- **Config file locations on disk in 1.21.1 NeoForge** (unchanged from Forge 1.20.1
  conceptually):
  - `ModConfig.Type.CLIENT` and `ModConfig.Type.COMMON` -> the instance-global
    `config/` directory (e.g. `config/apothic_tooltip_cleanup-client.toml`).
  - `ModConfig.Type.SERVER` -> per-world, under `<world>/serverconfig/`.
  - `ModConfig.Type.STARTUP` -> also `config/`, loaded very early.

  Since Apothic Tooltip Cleanup is a client-side display mod, `ModConfig.Type.CLIENT` is
  correct and its file stays in the global `config/` directory.

---

## 8. NeoForge 1.21.1 ModList and Dist

**Status: confirmed.**

- **`ModList`:** `net.neoforged.fml.ModList` (verified in `loader-4.0.42.jar`).
  `ModList.get()` is a static accessor and `boolean isLoaded(String modid)` exists with the
  same signature and behavior as Forge 1.20.1. The current `ApotheosisDetector` works as-is
  after changing the import from `net.minecraftforge.fml.ModList` to `net.neoforged.fml.ModList`.

- **`Dist`:** `net.neoforged.api.distmarker.Dist` (was `net.minecraftforge.api.distmarker.Dist`).
  Constants: `Dist.CLIENT`, `Dist.DEDICATED_SERVER`. Helpers like
  `dist.isClient()` / `dist.isDedicatedServer()` are unchanged.

- Related import moves the port will need:
  - `SubscribeEvent`: `net.neoforged.bus.api.SubscribeEvent`
  - `EventPriority`: `net.neoforged.bus.api.EventPriority`
  - `IEventBus`: `net.neoforged.bus.api.IEventBus`

---

## 9. Apotheosis 8.x integration entry points

**Status: confirmed (helper classes); see caveat on "formal API".**

Apotheosis 8.5.3 does **not** ship a separate, versioned API jar or an `api` package. The
de-facto integration surface is a set of `public` helper classes with `public static`
methods. They are stable enough to use, but they are not annotated as a guaranteed API, so
treat them as version-coupled (pin a minimum Apotheosis version - see section 11).

Using these is strongly preferable to tooltip-line string parsing for any feature that needs
the underlying item data - in particular the **rarity color override** feature.

### Rarity / affixes - `dev.shadowsoffire.apotheosis.affix.AffixHelper`

```java
public static DynamicHolder<LootRarity> getRarity(ItemStack stack);
public static Map<DynamicHolder<Affix>, AffixInstance> getAffixes(ItemStack stack);
public static Stream<AffixInstance> streamAffixes(ItemStack stack);
public static boolean hasAffixes(ItemStack stack);
public static Component getName(ItemStack stack);                 // the affixed display name
public static Component getModifiedStackName(ItemStack, Component);
```

For the rarity color override, the cleanest 1.21.1 implementation is:

```java
DynamicHolder<LootRarity> holder = AffixHelper.getRarity(stack);
if (holder.isBound()) {
    LootRarity rarity = holder.get();
    TextColor actualColor = rarity.color();              // Apotheosis' own color
    String id = holder.getId().getPath();                // "common".."mythic"
}
```

This removes the dependence on `getTag()` / NBT entirely, and `rarity.color()` even lets the
override read or respect the mod's true color without a hardcoded switch.

### Rarity registry - `dev.shadowsoffire.apotheosis.loot.RarityRegistry`

```java
RarityRegistry.INSTANCE.getValue(ResourceLocation);   // -> LootRarity
RarityRegistry.INSTANCE.getKey(LootRarity);           // -> ResourceLocation
RarityRegistry.INSTANCE.holder(ResourceLocation);     // -> DynamicHolder<LootRarity>
RarityRegistry.getSortedRarities();                   // -> List<LootRarity>, by sortIndex
```

### Sockets - `dev.shadowsoffire.apotheosis.socket.SocketHelper`

```java
public static int          getSockets(ItemStack stack);
public static SocketedGems getGems(ItemStack stack);          // List<GemInstance>
public static boolean      hasEmptySockets(ItemStack stack);
public static int          getFirstEmptySocket(ItemStack stack);
public static boolean      canSocketGemInItem(ItemStack item, ItemStack gem);
```

`GemInstance` (record) exposes `isValid()`, `getGem()`, `purity()`, `category()`,
`getSocketBonusTooltip(AttributeTooltipContext)`, and
`addInformation(Consumer<Component>, AttributeTooltipContext)`.

### Gems / "fits in" info - `dev.shadowsoffire.apotheosis.socket.gem.GemItem`

```java
public static DynamicHolder<Gem> getGem(ItemStack stack);
public static Purity             getPurity(ItemStack stack);
```

`GemInstance.canApplyTo(ItemStack)` answers "does this gem fit in this item" directly, and
`Gem` / `GemClass` describe the categories the gem accepts - so the "Fits In" relationship can
be queried instead of parsed from `socketable_into` lines if needed.

### Caveat

These classes are public and used internally by Apotheosis itself, but they are not a formal
SemVer-stable API. Method signatures have changed between major versions before (e.g. the
whole `adventure` package was deleted in 8.x). Calling them is fine for a tight version range;
just keep the soft-dependency presence checks (`ApotheosisDetector`) and consider catching
`LinkageError` around the first call site, the way `SocketCompactor` already guards its
reflection with `reflectionFailed`.

---

## 10. Parchment mappings for 1.21.1

**Status: confirmed.**

- Latest known-good Parchment for Minecraft 1.21.1: **`2024.11.17`**. This is the final
  Parchment release for 1.21.1 (later Parchment releases target 1.21.3, 1.21.4, etc.).
- Maven coordinates: `org.parchmentmc.data:parchment-1.21.1:2024.11.17`.
- Repo / docs: `https://github.com/ParchmentMC/Parchment` and
  `https://parchmentmc.org/docs/getting-started`.
- The 1.21.1 NeoForge MDK already pins this exact version in `gradle.properties`:
  `parchment_minecraft_version=1.21.1`, `parchment_mappings_version=2024.11.17`, consumed by
  the `neoForge { parchment { ... } }` block in `build.gradle`. No change needed.

---

## 11. Apotheosis 8.x dependency declaration format

**Status: confirmed.**

Read directly from `META-INF/neoforge.mods.toml` inside `Apotheosis-1.21.1-8.5.3.jar`.

Apotheosis 8.5.3 itself declares (its own dependency block, for reference):

```toml
[[dependencies.apotheosis]]
    modId="neoforge"
    type="required"
    versionRange="[21.1.187,)"

[[dependencies.apotheosis]]
    modId="placebo"
    type="required"
    versionRange="[9.9.1,)"
    ordering="AFTER"

[[dependencies.apotheosis]]
    modId="apothic_attributes"
    type="required"
    versionRange="[2.7.0,)"
    ordering="AFTER"

[[dependencies.apotheosis]]
    modId="apothic_spawners"
    type="required"
    versionRange="[1.3.0,)"
    ordering="AFTER"

[[dependencies.apotheosis]]
    modId="apothic_enchanting"
    type="required"
    versionRange="[1.5.2,)"
    ordering="AFTER"
```

**Placebo is still a transitive requirement.** For the 1.21.1-8.5.3 line, Apotheosis requires
**Placebo `[9.9.1,)`** (the latest 1.21.1 Placebo is `1.21.1-9.9.1`). Note that in 8.x,
Apotheosis also hard-requires three more Shadows_of_Fire mods that were not separate
dependencies in 1.20.1: `apothic_attributes` `[2.7.0,)`, `apothic_spawners` `[1.3.0,)`, and
`apothic_enchanting` `[1.5.2,)`. For an end user these all install automatically with
Apotheosis; for a dev runtime you must add them too (see section 12).

### Soft-dependency block for `neoforge.mods.toml` in the cleanup mod

Apothic Tooltip Cleanup only enhances Apotheosis when present, so declare it `optional`. Add
this to `src/main/templates/META-INF/neoforge.mods.toml` (the `${mod_id}` placeholder is
expanded by the `generateModMetadata` task):

```toml
[[dependencies.${mod_id}]]
    modId="apotheosis"
    type="optional"
    versionRange="[8.0.0,)"
    ordering="AFTER"
    side="CLIENT"
```

- `type="optional"` is the NeoForge term for a soft dependency (the others are `required`,
  `incompatible`, `discouraged`).
- `versionRange="[8.0.0,)"` means "Apotheosis 8.0.0 or later" - a half-open Maven range,
  inclusive lower bound, unbounded upper. This is the correct syntax for "X or later".
- `ordering="AFTER"` makes Apothic Tooltip Cleanup load after Apotheosis so its tooltip
  handlers run with Apotheosis already initialized.
- `side="CLIENT"` because this is a client display mod.

If the rarity color feature ends up calling `AffixHelper` directly (section 9), consider
raising the lower bound to the oldest 8.x version you actually test against, since the helper
classes are not a guaranteed-stable API.

If a soft dependency on Apothic Attributes is also wanted (the current code probes
`apothic_attributes`), add a parallel `optional` block with `modId="apothic_attributes"`.

---

## 12. CurseMaven syntax for fetching Apotheosis as a build dependency

**Status: confirmed (file ID and syntax). Recommendation: prefer the official maven.**

### CurseForge identifiers

- Apotheosis CurseForge project ID: **`248584`** (confirmed from the Gradle cache descriptor
  `curse.maven/apotheosis-248584`).
- `Apotheosis-1.21.1-8.5.3.jar` (NeoForge) CurseForge file ID: **`8102047`**, uploaded
  2026-05-17. (For reference: 8.5.2 = `7703848`, 8.5.1 = `7659395`, 8.5.0 = `7659222`.)

### CurseMaven dependency line

```gradle
repositories {
    exclusiveContent {
        forRepository { maven { url = "https://cursemaven.com" } }
        filter { includeGroup "curse.maven" }
    }
}

dependencies {
    compileOnly "curse.maven:apotheosis-248584:8102047"
}
```

The `curse.maven` coordinate format is `curse.maven:<slug>-<projectId>:<fileId>`. The slug
part (`apotheosis`) is cosmetic; the project ID and file ID are what resolve the artifact.

### compileOnly vs implementation

Use **`compileOnly`** for a soft dependency. `compileOnly` puts Apotheosis on the compile
classpath only - the mod builds against its classes but does not force it as a runtime or
published dependency, which is exactly what a soft/optional dependency needs. `implementation`
would make it a hard, published transitive dependency and would also place it on the dev
runtime classpath, which is wrong for an optional dep.

This advice holds for both NeoGradle and ModDevGradle. **Note: the 1.21.1 MDK in this project
uses ModDevGradle** (`id 'net.neoforged.moddev' version '2.0.141'` in `build.gradle`), not
NeoGradle.

To actually launch the dev client/`runClient` with Apotheosis loaded for manual testing, add
the mod (and its hard deps) to a runtime configuration as well. The MDK's `build.gradle`
already defines a `localRuntime` configuration that feeds the run classpath without being
published:

```gradle
dependencies {
    compileOnly  "curse.maven:apotheosis-248584:8102047"

    // Only needed to test in the dev client; Apotheosis 8.x hard-requires these:
    localRuntime "curse.maven:apotheosis-248584:8102047"
    localRuntime "curse.maven:placebo-<id>:<fileId>"
    localRuntime "curse.maven:apothic-attributes-898963:<fileId>"
    localRuntime "curse.maven:apothic-spawners-<id>:<fileId>"
    localRuntime "curse.maven:apothic-enchanting-<id>:<fileId>"
}
```

(CurseMaven does not resolve transitive dependencies, so each hard dep must be listed
explicitly with its own file ID.)

### Recommended alternative: the official Shadows_of_Fire maven

Because Shadows_of_Fire publishes a real Maven repository, the cleaner option is to skip
CurseMaven entirely and use the same maven this research downloaded the jar from:

```gradle
repositories {
    maven { url = "https://maven.shadowsoffire.dev/releases" }
}

dependencies {
    compileOnly "dev.shadowsoffire:Apotheosis:1.21.1-8.5.3"
}
```

This is the maven the original 1.20.1 project already used (the Gradle cache contains
`dev.shadowsoffire:Apotheosis:1.20.1-7.4.8` resolved from it). It has proper POM metadata, so
it can pull the dependency graph (Placebo etc.) more cleanly than CurseMaven, and it gives
stable, human-readable version strings. Use CurseMaven only as a fallback if a needed version
is ever missing from the official maven.

---

## At-a-glance status summary

| # | Topic                                   | Status              |
|---|-----------------------------------------|---------------------|
| 1 | Affix / rarity data components          | Confirmed           |
| 2 | Rarity registry                         | Confirmed           |
| 3 | Translation keys                        | Confirmed           |
| 4 | Socket structure                        | Confirmed (rendering path, keys, class moves); partially confirmed (exact Sigil-of-Socketing 3-socket cap behavior / 8.3.0 change) |
| 5 | NeoForge `ItemTooltipEvent`             | Confirmed           |
| 6 | Mod loader patterns                     | Confirmed           |
| 7 | `ModConfigSpec`                         | Confirmed           |
| 8 | `ModList` and `Dist`                    | Confirmed           |
| 9 | Apotheosis integration entry points     | Confirmed (public helper classes; no formal versioned API exists) |
| 10| Parchment mappings                      | Confirmed           |
| 11| Dependency declaration format           | Confirmed           |
| 12| CurseMaven syntax / file ID             | Confirmed           |

### Items that need follow-up confirmation

- **Section 4 - Sigil of Socketing cap.** The current `text.apotheosis.socket_limit`
  ("Max Sockets Applied: %s") line and the `apotheosis:sockets` integer component (range
  0..16) are confirmed. The specific claim that 8.3.0 introduced a 3-socket Sigil cap and
  changed the rendered line format could not be verified from the jar alone. To confirm,
  check the Apotheosis 8.3.0 changelog on CurseForge or read `AddSocketsRecipe` /
  `SigilOfSocketing` logic in the GitHub source.
- **No local 1.21.1 NeoForge test instance was found.** All research used the jar pulled from
  `maven.shadowsoffire.dev`. Before in-game testing of the port, a 1.21.1 NeoForge instance
  with Apotheosis 8.5.3 (plus Placebo 9.9.1, Apothic Attributes 2.7.0+, Apothic Spawners
  1.3.0+, Apothic Enchanting 1.5.2+) still needs to be created. Confirm where you want that
  instance to live.
