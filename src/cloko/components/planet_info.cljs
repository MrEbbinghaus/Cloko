(ns cloko.components.planet-info
  (:require [reagent.core :as r]
            [cloko.core :as core]))

(defn planet-info-component
  [state]
  (let [selected-planet (get-in state [:fe :selected-planet])
        planet (get-in state [:world :planets selected-planet])]
    [:div.panel.panel-default
     [:div.panel-heading "Planet information"]
     [:div.planet-info.panel-body (if planet
                                    [:table.table
                                     [:tr [:td "Position"] [:td (first selected-planet) ":" (second selected-planet)]]
                                     [:tr [:td "Owner"] [:td (:owner planet)]]
                                     [:tr [:td "Ships per turn"] [:td (:ships-per-turn planet)]]
                                     [:tr [:td "Stationed ships"] [:td (:ships planet)]]]
                                    [:p "No planet selected!"])]]))