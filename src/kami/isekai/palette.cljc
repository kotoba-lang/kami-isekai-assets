(ns kami.isekai.palette
  "Colour system for kami-isekai-assets. The default treatment is the late-90s
   Squaresoft 'artisan RPG' palette (Legend of Mana / SaGa Frontier 2 / Valkyrie
   Profile / FFT era): desaturated, paper-lifted watercolour tones rather than
   saturated cartoon primaries — low-poly-as-craft, not low-poly-as-cheap.
   `brainrot` is an explicit opt-in loud/saturated remix for callers who want
   the current internet-meme register instead; it is never applied by default.")

;; paper-lifted watercolour: desaturate toward a warm cream, then lift value a
;; touch — the same trick a watercolourist uses so pigment reads as painted on
;; paper rather than printed flat. `amount` 0..1, higher = more washed-out.
(defn watercolor
  ([rgb] (watercolor rgb 0.28))
  ([[r g b] amount]
   (let [paper [0.94 0.90 0.80]
         mix (fn [c p] (+ (* c (- 1.0 amount)) (* p amount)))]
     (mapv mix [r g b] paper))))

;; loud/saturated remix — opt-in only (see ns docstring). Boosts contrast
;; around the colour's OWN mean brightness (not a fixed 0.5) — boosting
;; around a fixed midpoint looked right for mid-tone inputs but backfired on
;; light ones: a pale skin tone's channels are all >0.5 already, so pushing
;; each toward 1.0 independently clamped them together and LOWERED
;; saturation vs. plain watercolor (found by actually computing max-min
;; spread per race, not just asserting not=). Boosting around the color's
;; own mean spreads channels apart relative to each other regardless of how
;; bright or dark the input is, so it can't collapse the same way.
(defn brainrot
  [[r g b]]
  (let [mean  (/ (+ r g b) 3.0)
        boost (fn [c] (max 0.0 (min 1.0 (+ (* (- c mean) 1.9) mean))))]
    (mapv boost [r g b])))

;; per-race base hues (skin/hide, hair/mane-or-accent, garment) — muted before
;; watercolor/brainrot is applied on top by the caller.
(def race-hues
  {:human      {:skin [0.86 0.72 0.60] :accent [0.30 0.20 0.14] :garment [0.34 0.30 0.46]}
   :hume       {:skin [0.86 0.72 0.60] :accent [0.30 0.20 0.14] :garment [0.34 0.30 0.46]} ;; FFT-lineage flavour alias of :human
   :elf        {:skin [0.88 0.80 0.68] :accent [0.86 0.82 0.40] :garment [0.28 0.44 0.34]}
   :dwarf      {:skin [0.78 0.60 0.48] :accent [0.50 0.34 0.20] :garment [0.42 0.28 0.18]}
   :orc        {:skin [0.44 0.54 0.30] :accent [0.20 0.16 0.10] :garment [0.30 0.24 0.18]}
   :goblin     {:skin [0.52 0.62 0.32] :accent [0.24 0.20 0.12] :garment [0.36 0.30 0.16]}
   :beastman   {:skin [0.62 0.48 0.38] :accent [0.36 0.26 0.18] :garment [0.32 0.24 0.30]}
   :kobold     {:skin [0.58 0.42 0.30] :accent [0.28 0.18 0.10] :garment [0.34 0.22 0.14]}
   :troll      {:skin [0.42 0.52 0.36] :accent [0.24 0.28 0.18] :garment [0.30 0.26 0.20]}
   :dragon-kin {:skin [0.36 0.24 0.46] :accent [0.70 0.20 0.24] :garment [0.20 0.16 0.28]}})

;; per-class accent overlay (worn over the race garment tone) — crown gold,
;; guild emerald, merchant amber, mage indigo, knight steel, princess rose.
(def class-hues
  {:adventurer   {:accent [0.58 0.42 0.24]}
   :knight       {:accent [0.55 0.58 0.62]}
   :mage         {:accent [0.30 0.24 0.52]}
   :merchant     {:accent [0.62 0.46 0.20]}
   :guild-master {:accent [0.22 0.46 0.34]}
   :king         {:accent [0.72 0.60 0.20]}
   :princess     {:accent [0.72 0.40 0.50]}
   :priest       {:accent [0.90 0.86 0.68]}})

(defn- to-int32
  "Coerce to the 32-bit signed integer domain bit-xor/bit-shift-left/
   unsigned-bit-shift-right operate in ON BOTH PLATFORMS — ClojureScript's
   bitwise ops implicitly ToInt32 their operands (ECMA-262 semantics: wrap
   at 2^32, then treat the top half as negative), but the JVM's bit-* fns
   run on genuine 64-bit longs with no such truncation. Confirmed this is a
   REAL divergence, not a hypothetical one: ran seeded-jitter through both
   bb (JVM) and nbb (real ClojureScript on Node) for identical large seeds
   and got different jitter values for anything beyond ±2^31 (e.g.
   9999999999 → 0.467 on the JVM, 0.507 in cljs). No currently-shipped
   preset's seed is large enough to hit this (kami.isekai.presets' (hash
   [race class]) values all stay within ±2^31), so nothing deployed is
   wrong today — but a .cljc fn should genuinely agree across platforms
   regardless of caller input, not by luck of which seeds happen to be used."
  [n]
  (let [m (bit-and n 0xffffffff)]
    (if (>= m 0x80000000) (- m 0x100000000) m)))

(defn- ushr32
  "unsigned-bit-shift-right within a 32-bit-wide field, matching JS's >>>. JS's >>>
   treats its left operand as an UNSIGNED 32-bit value and shifts within that
   32-bit field; the JVM's unsigned-bit-shift-right shifts a value's actual 64-bit
   long bit pattern — for a negative (sign-extended) input those are different
   physical bit patterns, so the two platforms shift different bits and can
   disagree even after to-int32 alone. Masking to 0xffffffff first confines the
   shift to the same 32-bit-wide unsigned field JS uses."
  [n amt]
  (unsigned-bit-shift-right (bit-and n 0xffffffff) amt))

(defn seeded-jitter
  "Deterministic small hue jitter from an integer seed, so N characters of the
   same race/class don't render identically. Pure — same seed → same jitter.
   Agrees across JVM Clojure and ClojureScript for ANY integer seed (see
   to-int32).

   :seed 0 is a fixed point of this XOR-shift construction and always
   returns 0.0 — and 0 is compose-character's default when no :seed is
   passed. This means calling compose-character repeatedly with no :seed
   gives every instance the exact same skin tone; pass distinct non-zero
   seeds (kami.isekai.presets does this via (hash [race class])) to get
   visual variety across multiple instances of the same race/class."
  [seed]
  ;; to-int32/ushr32 after every bit-shift-left/unsigned-bit-shift-right, not just
  ;; on the input: JS truncates to int32 (or uint32, for >>>) after EACH bitwise
  ;; operator; the JVM's bit-* fns run on 64-bit longs with no such truncation
  ;; until masked. Found by re-running the bb-vs-nbb comparison after each fix
  ;; attempt: masking only the entry point wasn't enough (2147483647 still
  ;; diverged — 0.9054 JVM vs 0.9214 cljs), and even masking every bit-shift-left
  ;; step still left unsigned-bit-shift-right disagreeing (it shifts the value's
  ;; real 64-bit bit pattern on the JVM vs. a 32-bit-wide field in JS — ushr32
  ;; closes that specific gap).
  (let [seed (to-int32 seed)
        s (to-int32 (bit-xor seed (to-int32 (bit-shift-left seed 13))))
        s (to-int32 (bit-xor s (ushr32 s 17)))
        s (to-int32 (bit-xor s (to-int32 (bit-shift-left s 5))))]
    (/ (double (mod s 200)) 1000.0)))              ;; 0.0 .. 0.199

(defn jitter-color [[r g b] seed]
  (let [j (seeded-jitter seed)]
    [(min 1.0 (+ r j)) g (min 1.0 (+ b (- j)))]))
