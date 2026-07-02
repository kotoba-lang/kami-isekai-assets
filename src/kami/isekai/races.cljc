(ns kami.isekai.races
  "Playable-race body plans for kami.isekai.chargen. Every race is a set of
   proportion/accent tweaks on the same humanoid silhouette (head + torso, two
   circles) except :dragon-kin, which adds wings. The tweaks are deliberately
   small and readable at kami.sprite2d scale (a handful of primitives) rather
   than illustrative — the same restraint the PS1-era watercolour sprites used.")

;; :ears        :round | :pointed | :tusked | :long
;; :stature     torso/head scale multiplier (dwarf/goblin small, orc/dragon-kin large)
;; :tail        true → a small accent arc at the back
;; :wings       true → two small back-mounted primitives (dragon-kin only)
;; :horns       true → two small head-top accents
(def races
  {:human      {:label "Human"     :ears :round   :stature 1.00 :tail false :wings false :horns false}
   :hume       {:label "Hume"      :ears :round   :stature 1.00 :tail false :wings false :horns false} ;; alias flavour, see palette.cljc
   :elf        {:label "Elf"       :ears :pointed :stature 1.00 :tail false :wings false :horns false}
   :dwarf      {:label "Dwarf"     :ears :round   :stature 0.78 :tail false :wings false :horns false}
   :orc        {:label "Orc"       :ears :round   :stature 1.15 :tail false :wings false :horns false :tusked true}
   :goblin     {:label "Goblin"    :ears :long    :stature 0.68 :tail false :wings false :horns false}
   :beastman   {:label "Beastman"  :ears :round   :stature 1.05 :tail true  :wings false :horns false}
   :dragon-kin {:label "Dragon-kin":ears :round   :stature 1.10 :tail true  :wings true  :horns true}})

(defn race [id] (get races id (:human races)))
