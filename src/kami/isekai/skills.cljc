(ns kami.isekai.skills
  "Skill/magic catalog — each entry is a kami.audio synth recipe (same shape
   as every game's :audio block: {:wave :freq :to :dur :gain}, no asset
   files) plus a kami :fx :burst particle spec. Drop an entry's :audio/:fx
   straight into a game's scene.edn under the matching key; :sfx-key names
   the key to use for both.")

(def skills
  {:fireball
   {:label "Fireball" :element :fire :sfx-key :fireball
    :audio {:wave "sawtooth" :freq 180 :to 620 :dur 0.22 :gain 0.22}
    :fx    {:n 22 :spd 3.4 :grav 0.10 :life 30 :size 6
            :colors ["#ff6a3d" "#ffae4a" "#ffe14a" "#3a1a0a"] :spark 0.5}}

   :ice-lance
   {:label "Ice Lance" :element :ice :sfx-key :ice-lance
    :audio {:wave "triangle" :freq 880 :to 1320 :dur 0.14 :gain 0.16}
    :fx    {:n 14 :spd 2.6 :grav 0.02 :life 34 :size 4
            :colors ["#bfe9ff" "#eaffff" "#5aa0e0"] :spark 0.4}}

   :holy-heal
   {:label "Holy Heal" :element :holy :sfx-key :holy-heal
    :audio {:wave "sine" :freq 660 :to 990 :dur 0.30 :gain 0.14}
    :fx    {:n 18 :spd 1.2 :grav -0.06 :life 46 :size 5
            :colors ["#fff7d0" "#ffe9a8" "#ffffff"] :spark 0.5}}

   :curse
   {:label "Curse" :element :dark :sfx-key :curse
    :audio {:wave "sawtooth" :freq 140 :to 60 :dur 0.40 :gain 0.20}
    :fx    {:n 16 :spd 1.4 :grav 0.03 :life 40 :size 5
            :colors ["#4a1a6a" "#1a0a2a" "#8a4ac0"] :spark 0.3}}

   :cheat-aura
   {:label "Cheat Aura" :element :op :sfx-key :cheat-aura
    :audio {:wave "sine" :freq 523 :to 1046 :dur 0.5 :gain 0.18}
    :fx    {:n 26 :spd 1.6 :grav -0.02 :life 60 :size 6
            :colors ["#ffe14a" "#ffffff" "#ffd27a"] :spark 0.6}}})

(defn skill [id] (get skills id))
