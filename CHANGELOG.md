# Changelog

## Unreleased

- Monster variety, now that every named keyword from the original request
  has landed something (shifting from "close a gap" to "add depth"):
  - Added race `:troll` (large, stature 1.35) and `compose-troll` (bare-
    handed brute, reuses the humanoid plan like goblin/orc/skeleton).
  - Added `compose-ghost` — a new standalone ethereal plan (soft glow,
    translucent body, wavy trailing hem, no legs), the archetypal dungeon
    floater alongside slime/wolf's standalone plans.
  - Added named elemental slime variants: `compose-slime-fire`,
    `compose-slime-ice`, `compose-slime-poison` (every isekai slime dungeon
    has a colour per floor).
  - Coverage: 10 races × 7 classes, 13 monsters (was 9×7, 8).
- Added `kami.isekai.tensei` — the genre's namesake moment (異世界転生, the
  one original-request keyword that had zero representation through 4
  rounds of coverage work). `compose-summoning-circle` (ground prop,
  concentric rings + radial rune ticks) + `transition` (the arrival flash —
  kami.audio + kami :fx, same shape as a skills entry).
- Added `kami.isekai.status` — `compute-stats` derives HP/MP/ATK/DEF/SPD/LUK
  from race + class + level, deterministic. 能力/チート were represented
  visually (skills, cheat-aura) but not mechanically — a "status window"
  with numbers is core to the genre. `:cheat? true` applies an 8× multiplier
  (the OP-protagonist trope as numbers). Deliberately kept separate from
  `compose-character` — mechanical data, not auto-merged into the visual
  output, same as `kami.isekai.skills` is its own opt-in catalog.
- Added `kami.isekai.equipment` — weapon/held-item silhouettes (sword,
  dagger, staff, bow, shield, scepter). `compose-character` now applies
  each class's default loadout automatically (knight → sword+shield, mage →
  staff, adventurer → dagger, king → scepter; `:equip? false` opts out) —
  bare-handed characters read as unfinished, this was the biggest visible
  maturity gap in the v1 catalog. Monster composers that reuse a humanoid
  class for the silhouette but don't literally fit the loadout (dragon,
  wyvern) explicitly opt out.
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
