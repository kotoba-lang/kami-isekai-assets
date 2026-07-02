# Changelog

## Unreleased

- **Motion variety**: the catalog had only ever used 2 of the engine's 4
  `:anim` kinds (`:pulse`/`:sway` — confirmed `:bob`/`:rot` are real via
  `goriketsu/scene.edn`). Fixed the two clearest mismatches:
  `compose-ghost`'s body/head/eyes now `:bob` (float) instead of `:pulse`
  (breathe) — a monster with no legs is the one place "alive and
  breathing" is definitionally wrong. `compose-summoning-circle`'s 8 rune
  ticks now share one `:rot`/`:pivot [0 0]` so the whole ring turns as a
  unit — a static magic circle read as a diagram, not a spell in progress.
  Verified via headless capture (3-frame time-lapse, not just `bb test`) —
  the rune ring visibly rotates. `bb test` 38/38 (was 35/35), including a
  check that the catalog now covers all 4 anim kinds.
- **Closed the loop on the drift-detection audit** (3rd consecutive round
  checking for this bug class): `kami.isekai.catalog`'s `monster-ids`/
  `structure-ids` are hand-maintained sets mirroring the actual
  `compose-*` functions in `monsters`/`structures`/`tensei` — the same
  drift shape that bit `status.cljc` twice. Added a JVM-reflection test
  (`ns-publics`, `bb test` runs on the JVM even though the library itself
  stays `.cljc`-portable) that compares `catalog/monster-ids` and
  `structure-ids` against the actual defined functions. Unlike the last 2
  rounds, this one found *no* existing drift — both sets were already
  accurate — but it's now a permanent guard instead of a one-time manual
  check, so a forgotten catalog entry on the next new monster fails loudly.
- **Fixed data drift**: `kami.isekai.status`'s `race-modifiers`/
  `class-modifiers` are a separate map from `kami.isekai.races`/`classes`
  (mechanical tuning vs. visual data) and had silently drifted — `:troll`
  (added 2 rounds ago) and `:priest` (added last round) were never given
  stat modifiers, so they silently computed as unmodified base stats. Now
  fixed with real tuning (troll: tanky/slow; priest: high MP, low ATK) and
  `compute-stats` throws on an unknown race/class (same fix as
  `races/race`/`classes/class`). `kami.isekai.equipment/class->weapons` had
  the same drift risk in reverse — `:priest` was *correctly* bare-handed by
  omission, but an omitted key and an explicit `[]` no-op identically, so
  "forgotten" and "intentionally bare" were indistinguishable; made the
  entry explicit. Added completeness tests (map keys == the full catalog
  id set) for `status`, `equipment`, and `palette` so a future new race/
  class can't silently ship half-wired again.
- **Fixed a silent-fallback footgun**: `races/race` and `classes/class`
  used to default to `:human`/`:adventurer` on an unknown id instead of
  erroring — a typo (`:elve` for `:elf`) silently shipped a wrong-but-valid
  character with no signal. Both now throw `ex-info` with the full list of
  known ids; `compose-character` propagates the failure. Verified no
  internal caller relied on the fallback (only chargen called these two
  fns, and always with a literal valid keyword) before making the change.
- Added class `:priest` — the dedicated healer every isekai party has,
  distinct from the offensive `:mage`. Bare-handed by default (no entry in
  `equipment/class->weapons` — a priest's tool is holy magic, not a blade),
  a new `:holy-symbol` accessory (chest-height pulsing cross). Pairs
  thematically with `kami.isekai.skills/:holy-heal`.
- Added `kami.isekai.catalog` — one-stop introspection over every race/
  class/skill/monster/structure id (`known-race?` etc., `summary`), for a
  character-creator UI or a script that wants to validate an id before
  calling `compose-character` without importing every namespace.
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
