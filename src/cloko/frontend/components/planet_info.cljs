(ns cloko.frontend.components.planet-info
  (:require [reagent.core :as r]
            [cloko.core :refer [game-state whose-turn]]
            [cloko.frontend.utils :refer [fe-state position-formater]]))

(defn planet-details [planet [x y] current-player]
  "Gets planet information, a vector of said planet and the current-player.
  Returns a hiccup table with these informations structured."
  [:table.table
   [:tbody
    [:tr [:td "Position"] [:td (position-formater [x y])]]
    [:tr [:td "Owner"] [:td (:owner planet)]]
    [:tr [:td "Ships per turn"] [:td (:ships-per-turn planet)]]
    (when (= current-player (:owner planet))
      [:tr [:td "Stationed ships"] [:td (:ships planet)]])]])

(defn planet-info-panel []
  "Returns a react component which displayes information about the selected planet"
  (fn []
    (let [state @game-state
          planet-position (:selected-planet @fe-state)
          planet (get-in state [:world :planets planet-position])
          current-player (whose-turn state)]
      [:div.panel.panel-default
       [:div.panel-heading "Planet information"]
       [:div.planet-info.panel-body (if planet
                                      (planet-details planet planet-position current-player)
                                      [:p "No planet selected!"])]])))