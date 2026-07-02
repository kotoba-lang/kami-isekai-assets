(ns kami.isekai.status
  "Stat/status-block generator — the isekai 'check your status window' trope
   (levels, HP/MP/ATK/DEF/SPD/LUK). Deliberately separate from chargen: this
   is mechanical data, not a visual, so it isn't auto-merged into
   compose-character's output the way equipment is — call it explicitly
   alongside chargen when a game wants numbers, same as a caller already
   picks kami.isekai.skills entries independently. Rendering a status window/
   HUD from this is up to the caller (kami :hud conventions, same as the rest
   of the catalog).")

(def base-stats {:hp 100 :mp 20 :atk 10 :def 10 :spd 10 :luk 10})

;; small, thematic nudges — not meant to be a balanced tabletop system, just
;; enough that a dwarf reads tankier and a mage reads squishier-but-magic.
(def race-modifiers
  {:human      {}
   :hume       {}
   :elf        {:mp 15 :atk -2}
   :dwarf      {:def 8 :spd -2}
   :orc        {:atk 8 :hp 10 :mp -8}
   :goblin     {:spd 4 :hp -10}
   :kobold     {:spd 6 :hp -15}
   :beastman   {:atk 4 :spd 4}
   :dragon-kin {:hp 30 :atk 10 :def 10 :mp 10}})

(def class-modifiers
  {:adventurer   {}
   :knight       {:def 10 :hp 20}
   :mage         {:mp 30 :atk -4 :def -4}
   :merchant     {:luk 10}
   :guild-master {:luk 6 :def 4}
   :king         {:hp 20 :atk 6 :def 6 :luk 10}
   :princess     {:mp 10 :luk 14}})

(defn- level-scale [level] (+ 1.0 (* 0.12 (dec level))))

;; the isekai "cheat protagonist" trope, mechanically: not just a visual
;; aura (kami.isekai.chargen/cheat-aura) but numbers that don't belong at
;; this level. 8x is deliberately absurd, not a tuned game-balance value —
;; callers building a real game should treat this as a starting point.
(def cheat-multiplier 8.0)

(defn compute-stats
  "{:race kw :class kw :level int? :cheat? bool?} → {:level :hp :mp :atk :def
   :spd :luk :cheat?}. Deterministic — no seed needed, race/class/level/cheat?
   fully determine the block."
  [{:keys [race class level cheat?] :or {level 1 cheat? false}}]
  (let [rm    (get race-modifiers race {})
        cm    (get class-modifiers class {})
        scale (level-scale level)
        mult  (if cheat? cheat-multiplier 1.0)
        stat  (fn [k] (long (Math/round (double (* mult scale (+ (get base-stats k) (get rm k 0) (get cm k 0)))))))]
    {:level level :hp (stat :hp) :mp (stat :mp) :atk (stat :atk)
     :def (stat :def) :spd (stat :spd) :luk (stat :luk) :cheat? cheat?}))
