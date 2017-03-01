(ns cloko.frontend.core
  (:require [cloko.core :as core]
            [reagent.core :as r]
            [cloko.frontend.utils :refer [fe-state]]
            [cloko.frontend.components.planet-info :refer [planet-info-panel]]
            [cloko.frontend.components.movement-info :refer [movement-info-panel]]
            [cloko.frontend.components.board :refer [board]]
            [cloko.frontend.components.send :refer [send-panel]]
            [cloko.frontend.components.save-load :refer [save-component load-component]]))

(def next-turn-btn [:div.col-xs-1 [:button.btn.btn-danger
                                   {:on-click #((reset! fe-state {}) (core/end-turn!))}
                                   "Next turn"]])

(defn whose-turn [state] [:div.col-xs-1 [:label.label.label-info.full-width (name (get (:players state) (:whoseTurn state)))]])

(defn app []
  (let [state @core/game-state]
    [:div.panel.panel-default
     [:div.panel-body
      [:div.row (whose-turn state)]
      [:div.row [:div.col-md-12.center [board]]]
      [:div.row
       [:div.col-sm-6 [planet-info-panel]]
       [:div.col-sm-6 [movement-info-panel]]]
      [:div.row [(fn []
                   (let [selected-planet (get-in state [:world :planets (get-in @fe-state [:selected-planet])])]
                     (if (and
                           (= (core/whose-turn state) (:owner selected-planet))
                           (pos? (:ships selected-planet)))
                       [:div.col-xs-12 [send-panel]])))]]
      [:div.row next-turn-btn]
      [:div.row
       [:div.col-sm-12
        [:div.panel.panel-default
         [:div.panel-heading
          [:a {:data-toggle "collapse" :href "#collapse1"} "Fold out"]]
         [:div#collapse1.panel-collapse.collapse
          [:div.panel-body
           [:div.col-sm-6 [save-component]]
           [:div.col-sm-6 [load-component]]]]]]]]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))