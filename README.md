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
;; => {:sprite [[:circle {...}] [:circle {...}] [:arc {...}] ...]
;;     :render/profile {:color [...] :w 0.8 :h 1.7 :emissive 0.15}
;;     :tags ["elf" "mage"]}
```

Drop `:sprite` straight into a game's `scene.edn` under `:sprites {:my-npc
[...]}}`, and `:render/profile` under `:render/profiles {:my-npc {...}}`.

```clojure
(require '[kami.isekai.monsters :as monsters])
(monsters/compose-slime)                       ; the archetypal starter monster
(monsters/compose-dragon {:seed 3})            ; boss-tier, menacing recolour

(require '[kami.isekai.skills :as skills])
(skills/skill :fireball)                       ; {:audio {...kami.audio recipe...} :fx {...burst...}}
```

## Catalog

- **Races** (`kami.isekai.races`) — human, hume (FFT-lineage flavour alias of
  human), elf, dwarf, orc, goblin, beastman, dragon-kin.
- **Classes** (`kami.isekai.classes`) — adventurer, knight, mage, merchant,
  guild-master, king, princess.
- **Monsters** (`kami.isekai.monsters`) — slime, goblin-raider, orc-brute,
  dragon (boss).
- **Skills/magic** (`kami.isekai.skills`) — fireball, ice-lance, holy-heal,
  curse, cheat-aura (the isekai overpowered-protagonist trope, as a
  composable golden halo — `kami.isekai.chargen/cheat-aura`, or pass
  `:cheat? true` to `compose-character`).

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
