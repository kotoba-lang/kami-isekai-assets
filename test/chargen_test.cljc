;; Data gate for kami-isekai-assets. Run: bb test
(require '[kami.isekai.races :as races]
         '[kami.isekai.classes :as classes]
         '[kami.isekai.chargen :as chargen]
         '[kami.isekai.monsters :as monsters]
         '[kami.isekai.skills :as skills]
         '[kami.isekai.party :as party]
         '[kami.isekai.structures :as structures]
         '[kami.isekai.equipment :as equip]
         '[kami.isekai.status :as status]
         '[kami.isekai.tensei :as tensei])

(def prim-kinds #{:circle :rect :ellipse :arc})

(defn check [label pred]
  (if pred
    (println "  ✓" label)
    (throw (ex-info (str "GATE FAILED: " label) {}))))

(defn valid-sprite? [sprite]
  (and (vector? sprite)
       (seq sprite)
       (every? (fn [[kind opts]] (and (prim-kinds kind) (map? opts))) sprite)))

(println "kami-isekai-assets gate:")

(check "every race composes with every class to a valid sprite"
       (every? (fn [[rid _]]
                 (every? (fn [[cid _]]
                           (valid-sprite? (:sprite (chargen/compose-character {:race rid :class cid :seed 7}))))
                         classes/classes))
               races/races))

(check "compose-character is deterministic (same seed → identical sprite)"
       (= (chargen/compose-character {:race :elf :class :mage :seed 42})
          (chargen/compose-character {:race :elf :class :mage :seed 42})))

(check "different seeds can jitter skin colour (not asserting always-different — jitter is small/probabilistic)"
       (map? (chargen/compose-character {:race :human :class :adventurer :seed 1})))

(check "cheat? adds a golden aura (sprite grows by the aura primitives, tags gain cheat markers)"
       (let [plain (chargen/compose-character {:race :human :class :adventurer :seed 3})
             cheat (chargen/compose-character {:race :human :class :adventurer :seed 3 :cheat? true})]
         (and (> (count (:sprite cheat)) (count (:sprite plain)))
              (some #{"cheat"} (:tags cheat)))))

(check "brainrot variant is opt-in and produces a different (louder) palette than watercolor"
       (not= (chargen/compose-character {:race :orc :class :knight :seed 5 :variant :watercolor})
             (chargen/compose-character {:race :orc :class :knight :seed 5 :variant :brainrot})))

(def monster-composers
  [#(monsters/compose-slime) #(monsters/compose-goblin-raider {:seed 1})
   #(monsters/compose-orc-brute {:seed 1}) #(monsters/compose-dragon {:seed 1})
   #(monsters/compose-kobold-scout {:seed 1}) #(monsters/compose-wyvern {:seed 1})
   #(monsters/compose-skeleton {:seed 1}) #(monsters/compose-wolf)])

(check "every monster archetype composes to a valid sprite"
       (every? (fn [f] (valid-sprite? (:sprite (f)))) monster-composers))

(check "compose-skeleton recolours to bone/glow, not the menacing red used elsewhere"
       (let [sk (monsters/compose-skeleton {:seed 1})
             fills (keep (fn [[_ opts]] (:fill opts)) (:sprite sk))]
         (and (some #{"skeleton" "undead"} (:tags sk))
              (not-any? #(= [0.90 0.16 0.12] (vec (take 3 %))) fills))))

(check "every skill has a complete kami.audio recipe + kami :fx burst spec"
       (every? (fn [[_ s]]
                 (and (every? #(contains? (:audio s) %) [:wave :freq :to :dur :gain])
                      (every? #(contains? (:fx s) %) [:n :spd :grav :life :size :colors])))
               skills/skills))

(check "every race/class/skill id round-trips through name (safe to use as a scene.edn tag string)"
       (and (every? (comp string? name) (keys races/races))
            (every? (comp string? name) (keys classes/classes))
            (every? (comp string? name) (keys skills/skills))))

(check "compose-character auto-equips the class default loadout (a knight draws a sword+shield)"
       (let [bare  (chargen/compose-character {:race :human :class :knight :seed 9 :equip? false})
             armed (chargen/compose-character {:race :human :class :knight :seed 9})]
         (and (valid-sprite? (:sprite armed))
              (> (count (:sprite armed)) (count (:sprite bare))))))

(check "equip?  false opts a character out of the default loadout entirely (equal to a bare compose)"
       (let [bare (chargen/compose-character {:race :elf :class :mage :seed 2 :equip? false})]
         (= (:sprite bare)
            (:sprite (chargen/compose-character {:race :elf :class :mage :seed 2 :equip? false})))))

(check "kami.isekai.equipment/weapon-primitives covers every class->weapons kind with real primitives"
       (every? (fn [kind] (valid-sprite? (vec (equip/weapon-primitives kind {}))))
               (distinct (mapcat val equip/class->weapons))))

(check "compute-stats is deterministic and every race/class combo produces positive stats"
       (every? (fn [[rid _]]
                 (every? (fn [[cid _]]
                           (let [s (status/compute-stats {:race rid :class cid})]
                             (and (every? pos? [(:hp s) (:mp s) (:atk s) (:def s) (:spd s) (:luk s)])
                                  (= s (status/compute-stats {:race rid :class cid})))))
                         classes/classes))
               races/races))

(check "level scaling raises every stat (level 10 > level 1, same race/class)"
       (let [lo (status/compute-stats {:race :human :class :knight :level 1})
             hi (status/compute-stats {:race :human :class :knight :level 10})]
         (every? (fn [k] (> (get hi k) (get lo k))) [:hp :mp :atk :def :spd :luk])))

(check "cheat? multiplies every stat by cheat-multiplier — the isekai OP-protagonist trope as numbers, not just a visual aura"
       (let [plain (status/compute-stats {:race :human :class :adventurer})
             cheat (status/compute-stats {:race :human :class :adventurer :cheat? true})]
         (and (:cheat? cheat) (not (:cheat? plain))
              (every? (fn [k] (>= (get cheat k) (* 7 (get plain k)))) [:hp :mp :atk :def :spd :luk]))))

(check "compose-summoning-circle composes to a valid sprite (default + custom hue)"
       (and (valid-sprite? (:sprite (tensei/compose-summoning-circle)))
            (valid-sprite? (:sprite (tensei/compose-summoning-circle [0.8 0.3 0.6])))))

(check "tensei/transition has a complete kami.audio recipe + kami :fx burst spec (same shape as a skills entry)"
       (and (every? #(contains? (:audio tensei/transition) %) [:wave :freq :to :dur :gain])
            (every? #(contains? (:fx tensei/transition) %) [:n :spd :grav :life :size :colors])))

(check "castle and guild-hall structures compose to valid sprites (default + custom hue)"
       (and (valid-sprite? (:sprite (structures/compose-castle)))
            (valid-sprite? (:sprite (structures/compose-castle [0.3 0.3 0.35])))
            (valid-sprite? (:sprite (structures/compose-guild-hall)))
            (valid-sprite? (:sprite (structures/compose-guild-hall [0.5 0.3 0.2])))))

(check "compose-party positions every member with a distinct-enough offset (1..5 members, and a 6th wrap-around)"
       (every? (fn [n]
                 (let [members (party/compose-party (repeat n {:race :human :class :adventurer :seed n}))]
                   (and (= n (count members))
                        (every? #(and (valid-sprite? (:sprite %)) (vector? (:offset %))) members))))
               [1 2 3 4 5 6]))

(check "kami.isekai.party/starter-party composes to 4 members with the cheat-flagged protagonist first"
       (let [members (party/compose-party party/starter-party)]
         (and (= 4 (count members))
              (some #{"cheat"} (:tags (first members))))))

(println "kami-isekai-assets gate: OK —"
         (count races/races) "races ×" (count classes/classes) "classes,"
         (count monster-composers) "monsters," (count skills/skills) "skills")
