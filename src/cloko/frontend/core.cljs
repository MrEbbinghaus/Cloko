(ns cloko.frontend.core
  (:require [cloko.core :as core]
            [reagent.core :as r]
            [cloko.frontend.utils :refer [fe-state]]
            [cloko.frontend.components.planet-info :refer [planet-info-panel]]
            [cloko.frontend.components.movement-info :refer [movement-info-panel]]
            [cloko.frontend.components.board :refer [board]]
            [cloko.frontend.components.send :refer [send-panel]]
            [cloko.frontend.components.save-load :refer [save-component load-component]]
            [cloko.frontend.components.init :refer [init-component]]
            [cloko.frontend.components.game-info :refer [game-info]]))

(def next-turn-btn [:div.col-xs-1 [:button.btn.btn-danger
                                   {:on-click #((reset! fe-state {}) (core/end-turn!))}
                                   "Next turn"]])

(defn cloko-app []
  "Returns a react component of the cloko app"
  (let [state @core/game-state]
    [:div.panel.panel-default
     [:div.panel-body
      [game-info]
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
           [:div.row
            [:div.col-sm-4 [save-component]]
            [:div.col-sm-4 [load-component]]
            [:div.col-sm-4 [init-component]]]]]]]]]]))

(defn ^:export main
  "Renders the react app on page load."
  []  ;; do not forget the export
  (reset! fe-state {})
  (core/init! 9 6 3 2)
  (r/render [cloko-app]
            (.getElementById js/document "app")))