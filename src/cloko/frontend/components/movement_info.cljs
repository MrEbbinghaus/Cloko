(ns cloko.frontend.components.movement-info
  (:require [cloko.core :refer [player-movements game-state]]
            [cloko.frontend.utils :refer [position-formater]]))

(defn display-movement [movement]
  "Returns a hiccup like vector structure which represents a table which displays all usefull informations above a single movement.
  Gets the movement."
  [:table.table.table-condensed
   [:tbody
    [:tr [:td "From: "] [:td (position-formater (:origin movement))]]
    [:tr [:td "To: "] [:td (position-formater (:target movement))]]
    [:tr [:td "Ships: "] [:td (:ships movement)]]
    [:tr [:td "Rounds until arrival: "] [:td (:rounds-until-arrival movement)]]]])

(defn movement-info-panel []
  "Returns a react component which displays multiple possible movements."
  (fn []
    (let [movements (player-movements @game-state)]
      [:div.panel.panel-default
       [:div.panel-heading "Movement information"]
       [:div.panel-body
        (if (empty? movements)
          [:p "No movements!"]
          [:ul.list-group
           (for [movement movements]
             ^{:key (rand)} [:li.list-group-item (display-movement movement)])])]])))