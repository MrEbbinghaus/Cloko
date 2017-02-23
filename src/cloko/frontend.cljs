(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]
            [cloko.components.planet-info :as planet-info]
            [cloko.components.movement-info :as movement-info]
            [cloko.components.board :as board]))


(def next-turn-btn [:div.col-xs-1 [:button.btn.btn-success {:on-click core/end-turn!} "Next turn"]])

(defn whose-turn [state] [:div.col-xs-1 [:label.label.label-info.full-width (name (get (:players state) (:whoseTurn state)))]])

(defn app []
  (let [state @core/game-state]
    [:div.panel-body
     [:div.row (whose-turn state)]
     [:div.row [:div.col-md-12.center (board/board state)]]
     [:div.row
      [:div.col-sm-6 (planet-info/planet-info-panel state)]
      [:div.col-sm-6 (movement-info/movement-info-panel state)]]
     [:div.row next-turn-btn]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))

