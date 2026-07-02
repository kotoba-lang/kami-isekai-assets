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
         '[kami.isekai.tensei :as tensei]
         '[kami.isekai.catalog :as catalog]
         '[kami.isekai.palette :as pal]
         '[clojure.string :as str])

(def prim-kinds #{:circle :rect :ellipse :arc})

(defn check [label pred]
  (if pred
    (println "  ✓" label)
    (throw (ex-info (str "GATE FAILED: " label) {}))))

(defn valid-sprite? [sprite]
  (and (vector? sprite)
       (seq sprite)
       (every? (fn [[kind opts]] (and (prim-kinds kind) (map? opts))) sprite)))

(defn throws? [f]
  (try (f) false (catch #?(:clj Exception :cljs :default) _ true)))

(println "kami-isekai-assets gate:")

(check "races/race and classes/class throw on an unknown id instead of silently falling back"
       (and (throws? #(races/race :not-a-real-race))
            (throws? #(classes/class :not-a-real-class))
            (= "Human" (:label (races/race :human)))))        ;; still works for a real id

(check "compose-character propagates that failure — a typo'd :race doesn't silently ship a wrong human"
       (throws? #(chargen/compose-character {:race :elve :class :mage})))

(check "the thrown ex-info's :known set matches kami.isekai.catalog's independent race/class-ids"
       (try (races/race :nope) false
            (catch #?(:clj Exception :cljs :default) e
              (= (:known (ex-data e)) (catalog/race-ids)))))

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
   #(monsters/compose-skeleton {:seed 1}) #(monsters/compose-wolf)
   #(monsters/compose-troll {:seed 1}) #(monsters/compose-ghost)
   #(monsters/compose-slime-fire) #(monsters/compose-slime-ice) #(monsters/compose-slime-poison)])

(check "every monster archetype composes to a valid sprite"
       (every? (fn [f] (valid-sprite? (:sprite (f)))) monster-composers))

(check "the three elemental slime variants have distinct hues and element tags"
       (let [fire (monsters/compose-slime-fire) ice (monsters/compose-slime-ice) poison (monsters/compose-slime-poison)]
         (and (some #{"fire"} (:tags fire)) (some #{"ice"} (:tags ice)) (some #{"poison"} (:tags poison))
              (distinct? (:sprite fire) (:sprite ice) (:sprite poison)))))

(check "compose-troll is bare-handed (no equipment) and larger than a human (stature-scaled torso radius)"
       (let [troll (monsters/compose-troll {:seed 1})
             human (chargen/compose-character {:race :human :class :adventurer :seed 1 :equip? false})
             troll-torso-r (get-in (first (:sprite troll)) [1 :r])
             human-torso-r (get-in (first (:sprite human)) [1 :r])]
         (and (some #{"troll"} (:tags troll))
              (> troll-torso-r human-torso-r))))

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

(check "priest is bare-handed by default (a healer's tool is holy magic, not a blade)"
       (let [p (chargen/compose-character {:race :human :class :priest :seed 1})
             bare (chargen/compose-character {:race :human :class :priest :seed 1 :equip? false})]
         (= (:sprite p) (:sprite bare))))

(check "priest's holy-symbol accessory + cloak add primitives beyond the bare humanoid base"
       (let [bare   (chargen/compose-character {:race :human :class :adventurer :seed 1 :equip? false})
             priest (chargen/compose-character {:race :human :class :priest :seed 1})]
         (> (count (:sprite priest)) (count (:sprite bare)))))

(check "kami.isekai.catalog validates known/unknown ids correctly, and summary counts match reality"
       (and (catalog/known-race? :elf) (not (catalog/known-race? :not-a-real-race))
            (catalog/known-class? :priest) (not (catalog/known-class? :not-a-real-class))
            (catalog/known-monster? :troll) (not (catalog/known-monster? :not-a-real-monster))
            (catalog/known-skill? :fireball) (not (catalog/known-skill? :not-a-real-skill))
            (catalog/known-structure? :castle) (not (catalog/known-structure? :not-a-real-structure))
            (= (:races (catalog/summary)) (count races/races))
            (= (:classes (catalog/summary)) (count classes/classes))
            (= (:skills (catalog/summary)) (count skills/skills))))

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

(check "equipment/class->weapons has an explicit entry for every catalog class id (same drift class as status.cljc — an
        omitted key and an explicit [] both no-op identically, so 'forgotten' and 'intentionally bare' are
        indistinguishable without this)"
       (= (catalog/class-ids) (set (keys equip/class->weapons))))

(check "palette/race-hues and class-hues have an entry for every catalog race/class id (currently complete — locking
        it in as a test so the next new race/class can't silently ship colourless)"
       (and (= (catalog/race-ids) (set (keys pal/race-hues)))
            (= (catalog/class-ids) (set (keys pal/class-hues)))))

;; catalog/monster-ids and structure-ids are hand-maintained sets (not
;; derived — .cljc portability rules out relying on ns-publics reflection
;; at the library's own runtime), which is exactly the drift shape that bit
;; status.cljc twice already. This test closes the loop with JVM-only
;; reflection at *test* time (bb runs on the JVM even though the library
;; itself must stay CLJS-portable) — if a future round adds compose-troll2
;; and forgets to update catalog.cljc, this fails instead of silently
;; leaving the catalog stale.
#?(:clj
   (defn- compose-fn-ids [ns-sym]
     (->> (ns-publics ns-sym)
          keys
          (map name)
          (filter #(str/starts-with? % "compose-"))
          (map #(keyword (subs % 8)))
          set)))

(check "catalog/monster-ids matches every compose-* fn actually defined in kami.isekai.monsters"
       #?(:clj (= (compose-fn-ids 'kami.isekai.monsters) catalog/monster-ids)
          :cljs true))

(check "catalog/structure-ids matches every compose-* fn actually defined across structures.cljc + tensei.cljc"
       #?(:clj (= (into (compose-fn-ids 'kami.isekai.structures) (compose-fn-ids 'kami.isekai.tensei))
                  catalog/structure-ids)
          :cljs true))

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

(check "status/race-modifiers and class-modifiers have an entry for every catalog race/class id (the drift bug: a
        'positive stats' check alone can't catch a KNOWN id silently missing its modifiers — an absent entry
        defaults to {} same as an empty-but-present one, so this checks key presence directly)"
       (and (= (catalog/race-ids) (set (keys status/race-modifiers)))
            (= (catalog/class-ids) (set (keys status/class-modifiers)))))

(check "compute-stats throws on an unknown race/class, same fix as races/race and classes/class"
       (and (throws? #(status/compute-stats {:race :not-a-real-race :class :adventurer}))
            (throws? #(status/compute-stats {:race :human :class :not-a-real-class}))))

(check "troll and priest got real stat tuning, not just a present-but-empty modifiers entry"
       (let [troll-stats (status/compute-stats {:race :troll :class :adventurer})
             human-stats (status/compute-stats {:race :human :class :adventurer})
             priest-stats (status/compute-stats {:race :human :class :priest})]
         (and (> (:hp troll-stats) (:hp human-stats))     ;; troll is tankier
              (> (:mp priest-stats) (:mp human-stats)))))  ;; priest is more magical

(check "compose-summoning-circle composes to a valid sprite (default + custom hue)"
       (and (valid-sprite? (:sprite (tensei/compose-summoning-circle)))
            (valid-sprite? (:sprite (tensei/compose-summoning-circle [0.8 0.3 0.6])))))

(check "the 8 rune ticks all share the same :rot/:pivot so they turn together as one ring, not drift independently
        (visually confirmed via headless capture — this locks the *shape* of that fix in, not just presence)"
       (let [runes (filter (fn [[_ opts]] (get-in opts [:anim :rot])) (:sprite (tensei/compose-summoning-circle)))]
         (and (= 8 (count runes))
              (apply = (map (fn [[_ opts]] (:anim opts)) runes)))))

(check "compose-ghost floats (:bob) rather than breathes (:pulse) — the one monster with no legs is the wrong place
        for the 'alive and breathing' effect used everywhere else"
       (let [g (monsters/compose-ghost)
             anim-kinds (set (mapcat (fn [[_ opts]] (keys (:anim opts))) (:sprite g)))]
         (and (contains? anim-kinds :bob) (not (contains? anim-kinds :pulse)))))

(check "the catalog now uses all 4 anim kinds the engine supports (pulse/bob/rot/sway — confirmed real via
        public/games/gftd/goriketsu/scene.edn), not just the 2 it started with (:pivot is a companion parameter
        of :rot, not a 5th kind, so it's excluded from this set on purpose)"
       (let [known-kinds #{:pulse :bob :rot :sway}
             anim-kinds-of (fn [sprite] (set (mapcat (fn [[_ opts]] (filter known-kinds (keys (:anim opts)))) sprite)))
             everything (concat (mapcat (fn [[r _]] (mapcat (fn [[c _]] (:sprite (chargen/compose-character {:race r :class c :seed 1})))
                                                             classes/classes))
                                         races/races)
                                 (:sprite (monsters/compose-ghost))
                                 (:sprite (tensei/compose-summoning-circle))
                                 (:sprite (structures/compose-castle)))]
         (= known-kinds (anim-kinds-of everything))))

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
