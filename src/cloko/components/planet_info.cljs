(ns cloko.components.planet-info
  (:require [reagent.core :as r]
            [cloko.core :as core]))

(defn planet-details [planet [x y] current-player]
  [:table.table
   [:tbody
    [:tr [:td "Position"] [:td x ":" y]]
    [:tr [:td "Owner"] [:td (:owner planet)]]
    [:tr [:td "Ships per turn"] [:td (:ships-per-turn planet)]]
    (when (= current-player (:owner planet))
      [:tr [:td "Stationed ships"] [:td (:ships planet)]])]])

(defn planet-info-panel
  [state]
  (let [planet-position (get-in state [:fe :selected-planet])
        planet (get-in state [:world :planets planet-position])
        current-player (core/whose-turn state)]
    [:div.panel.panel-default
     [:div.panel-heading "Planet information"]
     [:div.planet-info.panel-body (if planet
                                    (planet-details planet planet-position current-player)
                                    [:p "No planet selected!"])]]))