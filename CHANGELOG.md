# Changelog

All notable changes are listed here, newest first.

## v1.3.0 2026-06-17

### Changed
- kept the "When Socketed in:" header in compact gem mode
- preserved the original bonus colors in compact gem mode instead of forcing gold
- showed "When Socketed in:" inline on the ultra mode bonus bullet
- narrowed the affix sort options to alphabetical and default, dropping the rarity and type options that never worked
- logged a warning when the socket compactor cannot read Apotheosis internals instead of disabling silently

### Fixed
- fixed cooldown and stacking markers not hiding on nested gem bonuses like bloody arrow, treasure goblin, fortification, and leech block

## v1.2.2 2026-06-03

### Added
- added displayTest IGNORE_SERVER_VERSION to neoforge.mods.toml

### Changed
- unified gem category filtering across the full, compact, and ultra gem modes, accepting id and name forms
- moved isGem to TooltipMatcher

### Removed
- removed the rarity color override

### Fixed
- fixed gem bonus category filtering on socketed items
- fixed garbled marker characters on socket lines
- fixed prefix deletion not replacing the affixed item name

## v1.2.1-neoforge 2026-05-29

### Fixed
- fixed gem category filtering and gem detection

## v1.2.0-neoforge 2026-05-28

### Added
- added show, alt, and delete toggles to the hide features

### Changed
- defaulted to showing all affixes

### Fixed
- fixed the Hold Alt prompt rendering

## v1.1.1-neoforge 2026-05-27

### Changed
- flipped the affix extras hide default

## v1.1.0-neoforge 2026-05-26

### Added
- first NeoForge 1.21.1 release, ported from the Forge build
- added the hidden_gem_categories setting
- added affix cooldown and stacking marker hiding

### Fixed
- fixed the Hold Alt prompt not showing when only the durability line was hidden
- fixed gem Fits In bullets being truncated by the affix display limit
- fixed a config crash on null values
