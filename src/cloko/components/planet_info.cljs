(ns cloko.components.planet-info
  (:require [reagent.core :as r]
            [cloko.core :as core]))

(defn planet-info-component
  [state]
  (let [selected-planet (get-in state [:fe :selected-planet])
        planet (get-in state [:world :planets selected-planet])]
    [:div.planet-info (if planet
                        [:ul.list-group
                         [:li.list-group-item "Position: " (first selected-planet) ":" (second selected-planet)]
                         [:li.list-group-item "Owner: " (:owner planet)]
                         [:li.list-group-item "Ships per turn: " (:ships-per-turn planet)]
                         [:li.list-group-item "Stationed ships: " (:ships planet)]]
                        [:p "No planet selected!"])]))
