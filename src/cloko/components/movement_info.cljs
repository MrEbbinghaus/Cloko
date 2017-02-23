(ns cloko.components.movement-info
  (:require [reagent.core :as r]
    [cloko.core :as core]))

(defn display-movement [movement]
  [:table.table.table-condensed
   [:tbody
    [:tr [:td "From: "] [:td (str (:origin movement))]]
    [:tr [:td "To: "] [:td (str (:target movement))]]
    [:tr [:td "Ships: "] [:td (:ships movement)]]
    [:tr [:td "Rounds until arrival: "] [:td (:rounds-until-arrival movement)]]]])

(defn movement-info-panel
  [state]
  (let [movements (core/player-movements state)]
    [:div.panel.panel-default
     [:div.panel-heading "Movement information"]
     [:div.panel-body
      (if (empty? movements)
        [:p "No movements!"]
        [:ul.list-group
         (for [movement movements]
           ^{:key movement} [:li.list-group-item (display-movement movement)])])]]))