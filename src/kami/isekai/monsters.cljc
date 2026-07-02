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
