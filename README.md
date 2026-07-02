# kami-isekai-assets

**An EDN template library for isekai/tensei-genre RPG characters and effects —
composed primitives, not asset files.**

`kami.isekai.chargen/compose-character` turns `{:race :class :seed}` into the
exact `kami.sprite2d` primitive vector every [network-isekai](https://isekai.network)
game already uses (`[:circle/:rect/:ellipse/:arc {...}]`), plus a
`:render/profiles` fallback for the 3D renderer. No PNG/GLB/WAV files — every
character, monster, and skill effect is data you can read, tweak, and fork,
same as the rest of the catalog.

## Why no real images/3D meshes/voice audio

This library stays inside the engine's existing "composed primitives +
synthesised audio, no asset files" philosophy (see network-isekai's own
`:render/sprite2d` games) rather than generating illustrated art, meshes, or
TTS voice lines — those need the AI-generation backend (`generate.html`,
Modal GPU) and are a separate, costed step per asset, not something this
library does by default.

## Aesthetic

The default palette (`kami.isekai.palette/watercolor`) desaturates every hue
toward a warm paper tone — the late-90s Squaresoft "artisan RPG" register
(*Legend of Mana*, *SaGa Frontier 2*, *Valkyrie Profile*, *Final Fantasy
Tactics*): watercolour over cel-shaded, restraint over saturation, a handful
of primitives reading as a whole silhouette rather than an illustration.
`kami.isekai.palette/brainrot` is an explicit **opt-in** loud/saturated remix
for the rare case you want the current internet-meme register instead — it
is never applied unless you ask for `:variant :brainrot`.

## Usage

```clojure
(require '[kami.isekai.chargen :as chargen])

(chargen/compose-character {:race :elf :class :mage :seed 42})
;; => {:sprite [[:circle {...}] [:circle {...}] [:arc {...}] [:rect {...}] ...]  ; incl. a staff — see below
;;     :render/profile {:color [...] :w 0.8 :h 1.7 :emissive 0.15}
;;     :tags ["elf" "mage"]}
```

Drop `:sprite` straight into a game's `scene.edn` under `:sprites {:my-npc
[...]}}`, and `:render/profile` under `:render/profiles {:my-npc {...}}`.

Every class draws its default weapon loadout automatically (a knight gets a
sword+shield, a mage a staff, an adventurer a dagger, a king a scepter —
`kami.isekai.equipment/class->weapons`) — a bare-handed "knight" reads as
unfinished, not restrained. Pass `:equip? false` to opt out, or call
`kami.isekai.equipment/equip` yourself for a custom loadout.

```clojure
(require '[kami.isekai.monsters :as monsters])
(monsters/compose-slime)                       ; the archetypal starter monster
(monsters/compose-dragon {:seed 3})            ; boss-tier, menacing recolour
(monsters/compose-skeleton {:seed 1})          ; undead dungeon-tier, bone/glow recolour
(monsters/compose-wolf)                        ; standalone quadruped plan (pack monster)

(require '[kami.isekai.skills :as skills])
(skills/skill :fireball)                       ; {:audio {...kami.audio recipe...} :fx {...burst...}}

(require '[kami.isekai.party :as party])
(party/compose-party party/starter-party)      ; 4 members, each with a formation :offset [dx dy]
```

## Catalog

- **Races** (`kami.isekai.races`) — human, hume (FFT-lineage flavour alias of
  human), elf, dwarf, orc, goblin, kobold, beastman, dragon-kin.
- **Classes** (`kami.isekai.classes`) — adventurer, knight, mage, merchant,
  guild-master, king, princess.
- **Monsters** (`kami.isekai.monsters`) — slime, goblin-raider, orc-brute,
  kobold-scout, wyvern, dragon (boss), skeleton (undead recolour), wolf
  (standalone quadruped plan, pack monster).
- **Skills/magic** (`kami.isekai.skills`) — fireball, ice-lance, holy-heal,
  curse, wind-blade, earth-spike, teleport, summon, cheat-aura (the isekai
  overpowered-protagonist trope, as a composable golden halo —
  `kami.isekai.chargen/cheat-aura`, or pass `:cheat? true` to
  `compose-character`).
- **Party formation** (`kami.isekai.party`) — `compose-party` arranges a list
  of character specs into a classic front-line/back-line RPG formation
  (1–5 dedicated slots, wraps beyond that); `starter-party` is the
  archetypal cheat-protagonist + knight + mage + rogue starter lineup.
- **Structures** (`kami.isekai.structures`) — world-decoration props, not
  characters: `compose-castle` (keep + flanking turrets + banner) and
  `compose-guild-hall` (the adventurer's-guild storefront), both taking an
  optional palette-hue override.
- **Equipment** (`kami.isekai.equipment`) — weapon/held-item silhouettes
  (sword, dagger, staff, bow, shield, scepter); `equip`/`equip-for-class`
  compose one or more onto any character's `:sprite`. `compose-character`
  applies the class default loadout automatically.

`bb gen-presets --out <dir>` writes a curated slice of the race×class /
monster catalog as standalone `character.edn` files — see
`scripts/gen_presets.clj`. network-isekai's default Asset Hub presets
(`isekai.network/assets.html`) are generated this way.

## Develop

```bash
bb test           # data gate: every race × class × monster × skill composes to valid data
bb gen-presets --out /tmp/isekai-presets
```

## License

Public domain (matches network-isekai's game/asset data).
