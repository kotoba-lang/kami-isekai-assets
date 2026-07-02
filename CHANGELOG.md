# Changelog

## Unreleased

- Added `kami.isekai.structures` — `compose-castle` (keep + flanking turrets
  + banner) and `compose-guild-hall` (adventurer's-guild storefront), the
  world-decoration props (お城/ギルド) that weren't covered by chargen's
  character-only composers. Same output shape ({:sprite ... :render/profile
  ... :tags ...}), optional palette-hue override.
- Added `kami.isekai.party` — `compose-party` arranges character specs into a
  1–5 slot front-line/back-line RPG formation; `starter-party` is the
  archetypal cheat-protagonist/knight/mage/rogue lineup.
- Added races: `:kobold`.
- Added monsters: `compose-kobold-scout`, `compose-wyvern` (lean, bare
  dragon-kin, no crown/cloak), `compose-skeleton` (undead recolour — bone/
  glow instead of the menacing red eyes), `compose-wolf` (standalone
  quadruped plan, pack monster).
- Added skills: `:wind-blade`, `:earth-spike`, `:teleport`, `:summon`.
- Coverage: 9 races × 7 classes, 8 monsters, 9 skills (was 8×7, 4, 5).

## v1 (2026-07-02)

Initial release — races (human/hume/elf/dwarf/orc/goblin/beastman/dragon-kin),
classes (adventurer/knight/mage/merchant/guild-master/king/princess),
monsters (slime/goblin-raider/orc-brute/dragon), skills (fireball/ice-lance/
holy-heal/curse/cheat-aura), watercolour Squaresoft-artisan default palette,
opt-in brainrot variant.
