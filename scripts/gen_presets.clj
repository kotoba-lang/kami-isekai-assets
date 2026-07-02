;; Writes a curated set of character/monster presets as standalone EDN files —
;; one representative slice across the race/class/monster catalog, not every
;; combination (56 race×class pairs is a browsing catalog, not a starter pack).
;; Run: bb gen-presets --out ../../gftdcojp/network-isekai/public/assets/isekai
(require '[kami.isekai.chargen :as chargen]
         '[kami.isekai.monsters :as monsters]
         '[clojure.java.io :as io]
         '[clojure.pprint :as pp])

(def presets
  (concat
    (for [[race class] [[:elf :mage] [:dwarf :guild-master] [:orc :knight]
                         [:human :adventurer] [:beastman :merchant]
                         [:hume :knight] [:goblin :adventurer]
                         [:dragon-kin :king] [:elf :princess]]]
      {:id (str (name race) "-" (name class))
       :compose #(chargen/compose-character {:race race :class class :seed (hash [race class])})})
    [{:id "op-protagonist" :compose #(chargen/compose-character {:race :human :class :adventurer :seed 999 :cheat? true})}
     {:id "slime" :compose monsters/compose-slime}
     {:id "goblin-raider" :compose #(monsters/compose-goblin-raider {:seed 1})}
     {:id "orc-brute" :compose #(monsters/compose-orc-brute {:seed 2})}
     {:id "dragon-boss" :compose #(monsters/compose-dragon {:seed 3})}]))

(defn- round3 [x] (/ (Math/round (* x 1000.0)) 1000.0))

(defn- clean-floats
  "Round doubles to 3dp so generated EDN reads like the hand-authored scenes
   elsewhere in the catalog, not like raw floating-point arithmetic output."
  [x]
  (cond
    (double? x)  (round3 x)
    (map? x)     (into {} (map (fn [[k v]] [k (clean-floats v)]) x))
    (vector? x)  (mapv clean-floats x)
    (seq? x)     (map clean-floats x)
    :else        x))

(defn write-preset! [out-dir {:keys [id compose]}]
  (let [dir (str out-dir "/" id)
        _ (io/make-parents (str dir "/character.edn"))
        data (clean-floats (compose))]
    (spit (str dir "/character.edn") (with-out-str (pp/pprint data)))
    (str dir "/character.edn")))

(defn -main [& args]
  (let [opts (apply hash-map args)
        out  (get opts "--out" "/tmp/isekai-presets")]
    (doseq [p presets]
      (println "wrote" (write-preset! out p)))
    (println (count presets) "presets written to" out)))

(apply -main *command-line-args*)
