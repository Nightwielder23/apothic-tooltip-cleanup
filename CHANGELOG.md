# Changelog

All notable changes are listed here, newest first.

## v1.2.2 2026-06-03

### Added
- added displayTest IGNORE_SERVER_VERSION to mods.toml

### Changed
- unified gem category filtering across the full, compact, and ultra gem modes, accepting id and name forms
- moved isGem to TooltipMatcher
- aligned CI triggers to push and pull_request

### Removed
- removed the rarity color override

### Fixed
- fixed gem bonus category filtering on socketed items
- fixed garbled marker characters on socket lines
- fixed prefix deletion not replacing the affixed item name

## v1.2.1 2026-05-29

### Fixed
- fixed gem category filtering and gem detection

## v1.2.0 2026-05-28

### Added
- added show, alt, and delete toggles to the hide features

### Changed
- defaulted to showing all affixes

### Fixed
- fixed the Hold Alt prompt rendering

## v1.1.1 2026-05-27

### Added
- added the hidden_gem_categories setting, including filtering in full mode
- added star bullet matching alongside dot bullets

### Changed
- showed the Hold Alt prompt whenever any line is hidden, including the durability line
- flipped the affix extras hide default

### Fixed
- fixed gem categories being dropped by affix truncation
- fixed affix extras not stripping cooldown and stacking markers

## v1.0.0 2026-05-07

### Added
- first release
- affix list truncation with Hold Alt to expand
- gem tooltip modes: full, compact, ultra, and hidden
- empty socket merging, including mixed sockets
- hiding for the affix summary, durability, source, and potion description lines
- affix sorting and prefix cleanup
