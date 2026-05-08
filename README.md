# Apothic Tooltip Cleanup

A client-side Forge 1.20.1 mod that cleans up the affix and gem tooltips Apotheosis adds to weapons, armor, and raw gems. Compatible with Apotheosis, Apotheotic Additions, and Apothic Attributes out of the box. Everything is configurable, every feature can be turned off.

## features

- Truncate affix lists, hold Alt to expand. Configurable to show all, show top N (default 3), or hide all unless Alt is held.
- Compact gem tooltips on raw gems. Configurable modes: full, compact, ultra, hidden.
- Merge empty sockets on socketed items into one summary line. Mixed sockets are supported: filled gems still render, with the empty count shown below.
- Hide the bonus durability line.
- Hide the Apotheosis affix summary block (Cold/Fire/HP%/Spell Resistance lines).
- Hide the affix source line.
- Custom rarity color overrides for all six tiers (Common, Uncommon, Rare, Epic, Mythic, Ancient). Esoteric (Apotheotic Additions) maps to Ancient via namespace fallback.
- Strip prefixes from affix names.
- Sort affix lines by rarity, alphabetical, type, or default.
- Hide individual affixes by translation key prefix.
- Hide potion-style affix descriptions.

## configuration

All toggles are in `config/apothic_tooltip_cleanup-client.toml`. The file regenerates from defaults if deleted.

## config reference

### affix display

`affix_display_mode` controls how affixes show on tooltips. Three options: `all` shows every affix, `top_n` shows the first N with Alt to expand the rest (default), `alt_only` hides every affix unless Alt is held.

`affix_visible_count` is the N for `top_n` mode. Default 3.

`affix_sort_order` is one of `default`, `rarity`, `alphabetical`, `type`.

`clean_affix_prefixes` strips prefixes from affix names. Off by default.

`hidden_affix_ids` is a list of translation key prefixes for affixes you want hidden entirely.

### affix tooltip lines

`hide_source_line` hides the affix source line.

`disable_summarization` hides the Apotheosis summary block (Cold/Fire/HP%/Spell Resistance).

`hide_durability_bonus` hides the "ignores X% of durability damage" line.

`disable_potion_descriptions` hides potion-style affix descriptions.

### gem display

`gem_tooltip_mode` controls raw gem tooltips. `full` keeps Apotheosis's original layout. `compact` (default) strips headers and the Unique tag, keeps the bullets, and removes "level to existing" wording. `ultra` puts categories on one line and bonuses on one line. `hidden` removes all gem info.

### sockets

`merge_empty_sockets` collapses empty sockets into one summary line. Filled gems still render normally; only the empty rows get merged. Default on.

`hide_apoth_marker` hides the `APOTH_REMOVE_MARKER` literal if Apotheosis lets it leak through. Don't enable this unless you actually see it, since it can hide the socket UI on socketed items.

### rarity colors

`rarity_colors_enabled` toggles custom rarity colors. When off (default), vanilla Apotheosis colors are used. The six color values use hex format `0xRRGGBB`. Apotheotic Additions's "Esoteric" rarity falls back to the Ancient color via namespace mapping.

## compatibility

- Apotheosis (soft dependency, the mod does nothing without it)
- Apotheotic Additions (optional, supported)
- Apothic Attributes (optional, supported)
- Fallen Gems & Affixes (tested compatible)
- May not visually compact empty sockets on packs that ASM-patch Apotheosis's socket renderer beyond what was tested.

## license

MIT, by Nightwielder23.