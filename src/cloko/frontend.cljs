(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]
            [cloko.components.planet-info :refer [planet-info-panel]]
            [cloko.components.movement-info :refer [movement-info-panel]]
            [cloko.components.board :as board]
            [cloko.components.send :as send]))

(defonce fe-state (r/atom {}))
(defn reset-fe! []
  (swap! core/game-state assoc :fe {}))

(def next-turn-btn [:div.col-xs-1 [:button.btn.btn-danger
                                   {:on-click #((reset-fe!) (core/end-turn!))}
                                   "Next turn"]])

(defn whose-turn [state] [:div.col-xs-1 [:label.label.label-info.full-width (name (get (:players state) (:whoseTurn state)))]])

(defn app []
  (let [state @core/game-state
        selected-planet (get-in state [:world :planets (get-in state [:fe :selected-planet])])]
    [:div.panel-body
     [:div.row (whose-turn state)]
     [:div.row [:div.col-md-12.center (board/board state)]]
     [:div.row
      [:div.col-sm-6 [planet-info-panel]]
      [:div.col-sm-6 [movement-info-panel]]]
     [:div.row (if (and
                       (= (core/whose-turn state) (:owner selected-planet))
                       (pos? (get-in state [:world :planets (get-in state [:fe :selected-planet]) :ships])))
                 [:div.col-xs-12 [send/send-panel state]]
                 (.log js/console))]
     [:div.row next-turn-btn]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))
