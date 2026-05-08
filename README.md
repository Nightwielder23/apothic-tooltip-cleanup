# Apothic Tooltip Cleanup

A client-side Forge 1.20.1 mod that cleans up the affix and gem tooltips Apotheosis adds to weapons, armor, and raw gems. Compatible with Apotheosis, Apotheotic Additions, and Apothic Attributes out of the box. Everything is configurable, every feature can be turned off.

## features

- Truncate affix lists with Hold Alt to expand. Configurable: show all, show top N (default 3), or hide all unless Alt is held.
- Compact gem tooltips on raw gems. Configurable modes: full, compact, tight, ultra, hidden.
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

## compatibility

- Apotheosis (required)
- Apotheotic Additions (optional, supported)
- Apothic Attributes (optional, supported)
- Fallen Gems & Affixes (tested compatible)
- May not visually compact empty sockets on packs that ASM-patch Apotheosis's socket renderer beyond what was tested.

## license

MIT, by Nightwielder23.