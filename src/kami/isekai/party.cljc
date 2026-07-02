(ns kami.isekai.party
  "Party formation — the isekai adventuring-party trope. `compose-party` takes
   an ordered list of character specs (kami.isekai.chargen/compose-character
   args) and returns each composed character plus a world-space :offset
   [dx dy], arranged in a classic front-line/back-line RPG formation (tank
   front-centre, flanks either side, support at the back). Offsets are in the
   same world units as everything else in the catalog (kami.sprite2d), scaled
   to roughly the spacing network-isekai games already use between entities
   (nocturne's buskers sit ~400-800 units apart) — a caller adds these to a
   spawn point and passes them straight to set-position!."
  (:require [kami.isekai.chargen :as chargen]))

;; formation slots by party size (index → world offset from the party's
;; anchor point). 1-4 members covers the classic isekai starter party
;; (protagonist + healer + tank + rogue); a 5th falls back to a second rank.
(def ^:private formations
  {1 [[0 0]]
   2 [[-160 0] [160 0]]
   3 [[0 -180] [-190 120] [190 120]]
   4 [[-190 -180] [190 -180] [-190 120] [190 120]]
   5 [[0 -260] [-220 -60] [220 -60] [-140 200] [140 200]]})

(defn- formation-for [n]
  (get formations n (get formations 5)))

(defn compose-party
  "specs: a vector of kami.isekai.chargen/compose-character arg maps, front-
   to-back / tank-to-support order. Returns a vector of
   {:sprite [...] :render/profile {...} :tags [...] :offset [dx dy]} —
   one per spec, positioned in a formation. Max 5 members get a dedicated
   slot; beyond that the 5-member formation repeats (safe, just overlapping)."
  [specs]
  (let [n (count specs)
        offsets (formation-for n)]
    (mapv (fn [spec [dx dy]] (assoc (chargen/compose-character spec) :offset [dx dy]))
          specs
          (if (<= n 5) offsets (cycle (formation-for 5))))))

(def starter-party
  "The archetypal isekai starter party spec — protagonist (cheat-flagged),
   knight tank, mage support, adventurer rogue. Pass straight to
   compose-party, or use as a template."
  [{:race :human :class :adventurer :cheat? true :seed 1}
   {:race :dwarf :class :knight     :seed 2}
   {:race :elf   :class :mage       :seed 3}
   {:race :beastman :class :adventurer :seed 4}])
