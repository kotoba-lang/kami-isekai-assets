(ns kami.isekai.monsters
  "Monster/enemy archetypes — reuses kami.isekai.chargen's humanoid body plan
   for goblin/orc/dragon (recoloured menacing: less watercolor wash, hotter
   eye accent) and adds a standalone :slime blob plan, since a slime has no
   humanoid silhouette to compose onto."
  (:require [kami.isekai.chargen :as chargen]
            [kami.isekai.palette :as pal]))

(defn- eyes [r fill]
  [[:circle {:dx (- (* r 0.32)) :dy (- (* r 0.05)) :r (* r 0.14) :fill fill}]
   [:circle {:dx    (* r 0.32)  :dy (- (* r 0.05)) :r (* r 0.14) :fill fill}]])

(defn compose-slime
  "A single gooey blob — the archetypal isekai starter monster. `hue` picks
   the elemental flavour (green default, red=fire, blue=ice, purple=poison)."
  ([] (compose-slime [0.42 0.78 0.40]))
  ([hue]
   (let [r 130]
     {:sprite [[:ellipse {:dx 0 :dy 0 :rx r :ry (* r 0.82) :fill (conj (pal/watercolor hue 0.08) 0.88)
                          :anim {:pulse [0.10 1.6]}}]
               [:ellipse {:dx 0 :dy (* r -0.18) :rx (* r 0.55) :ry (* r 0.30) :fill (conj (pal/watercolor hue -0.1) 0.5)}]
               [:circle  {:dx (- (* r 0.28)) :dy (- (* r 0.05)) :r (* r 0.10) :fill [0.08 0.08 0.10]}]
               [:circle  {:dx    (* r 0.28)  :dy (- (* r 0.05)) :r (* r 0.10) :fill [0.08 0.08 0.10]}]]
      :render/profile {:color hue :w 1.0 :h 0.7 :emissive 0.25}
      :tags ["slime" "monster"]})))

(defn- menacing [base]
  (assoc base :sprite
    (into (:sprite base) (eyes 70 [0.90 0.16 0.12]))))

(defn compose-goblin-raider [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :goblin :class :adventurer :seed seed :variant :watercolor})
      menacing
      (update :tags conj "monster" "raider")))

(defn compose-orc-brute [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :orc :class :adventurer :seed seed :variant :watercolor})
      menacing
      (update :tags conj "monster" "brute")))

(defn compose-dragon [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :dragon-kin :class :knight :seed seed :variant :watercolor})
      menacing
      (update :tags conj "monster" "boss" "dragon")))

(defn compose-kobold-scout [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :kobold :class :adventurer :seed seed :variant :watercolor})
      menacing
      (update :tags conj "monster" "scout")))

(defn compose-wyvern
  "A leaner, feral dragon-kin — no crown/cloak (bare :adventurer class), just
   the wings/tail/horns silhouette. A mid-tier flyer, not a boss."
  [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :dragon-kin :class :adventurer :seed seed :variant :watercolor})
      menacing
      (update :tags conj "monster" "flyer" "wyvern")))

(defn- undead
  "Recolours a composed character's torso/head fills to a pale bone tone and
   swaps the menacing red eyes for a cold glow — the isekai dungeon staple."
  [base]
  (let [bone [0.82 0.80 0.72]
        glow [0.45 0.92 0.72]
        recolor (fn [[kind opts]]
                   [kind (cond-> opts (:fill opts) (assoc :fill (conj (vec bone) (nth (:fill opts) 3 1.0))))])]
    (-> base
        (update :sprite (fn [sprite] (into (mapv recolor sprite) (eyes 70 glow))))
        (assoc-in [:render/profile :color] bone))))

(defn compose-skeleton
  "An undead humanoid — the archetypal isekai dungeon-tier monster. Reuses the
   human body plan (bare, no class accessory) recoloured bone-pale with a
   cold glowing eye accent instead of the menacing red used elsewhere."
  [{:keys [seed] :or {seed 0}}]
  (-> (chargen/compose-character {:race :human :class :adventurer :seed seed :variant :watercolor})
      undead
      (update :tags conj "monster" "undead" "skeleton")))

(defn compose-wolf
  "A standalone quadruped plan (no humanoid silhouette to compose onto, same
   reasoning as compose-slime) — a lean body, two ears, and a tail, iconic
   rather than anatomical. `hue` picks the pelt tone (grey default)."
  ([] (compose-wolf [0.42 0.40 0.44]))
  ([hue]
   (let [tone (pal/watercolor hue)
         dark (pal/watercolor (mapv #(* % 0.5) hue))]
     {:sprite [[:ellipse {:dx 0 :dy 10 :rx 150 :ry 78 :fill tone}]
               [:circle  {:dx 118 :dy -34 :r 58 :fill tone}]
               [:arc     {:dx 96  :dy -78 :r 22 :a0 0.2 :a1 2.6 :w 10 :stroke dark}]
               [:arc     {:dx 148 :dy -78 :r 22 :a0 0.2 :a1 2.6 :w 10 :stroke dark}]
               [:arc     {:dx -128 :dy 6 :r 60 :a0 0.9 :a1 2.6 :w 16 :stroke dark
                          :anim {:sway [0.06 1.2]}}]
               [:circle  {:dx 136 :dy -40 :r 8 :fill [0.92 0.86 0.30]}]]
      :render/profile {:color tone :w 1.4 :h 0.7 :emissive 0.1}
      :tags ["wolf" "monster" "pack"]})))
