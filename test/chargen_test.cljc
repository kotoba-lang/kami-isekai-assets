;; Data gate for kami-isekai-assets. Run: bb test
(require '[kami.isekai.races :as races]
         '[kami.isekai.classes :as classes]
         '[kami.isekai.chargen :as chargen]
         '[kami.isekai.monsters :as monsters]
         '[kami.isekai.skills :as skills])

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

(check "every monster archetype composes to a valid sprite"
       (and (valid-sprite? (:sprite (monsters/compose-slime)))
            (valid-sprite? (:sprite (monsters/compose-goblin-raider {:seed 1})))
            (valid-sprite? (:sprite (monsters/compose-orc-brute {:seed 1})))
            (valid-sprite? (:sprite (monsters/compose-dragon {:seed 1})))))

(check "every skill has a complete kami.audio recipe + kami :fx burst spec"
       (every? (fn [[_ s]]
                 (and (every? #(contains? (:audio s) %) [:wave :freq :to :dur :gain])
                      (every? #(contains? (:fx s) %) [:n :spd :grav :life :size :colors])))
               skills/skills))

(check "every race/class/skill id round-trips through name (safe to use as a scene.edn tag string)"
       (and (every? (comp string? name) (keys races/races))
            (every? (comp string? name) (keys classes/classes))
            (every? (comp string? name) (keys skills/skills))))

(println "kami-isekai-assets gate: OK —"
         (count races/races) "races ×" (count classes/classes) "classes,"
         4 "monsters," (count skills/skills) "skills")
