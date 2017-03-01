(ns cloko.frontend.components.init
  (:require [reagent.core :refer [atom]]
            [cloko.core :refer [init!]]
            [cljs.reader :refer [read-string]]))

(defn init-component []
  (fn []
    (let [internal-state (atom {:x-size 9
                                :y-size 9
                                :neutral 11
                                :player 5})]
      [:div.panel.panel-default
       [:div.panel-heading "Initalize a new game!"]
       [:div.panel-body
        [:div.input-group
         [:span.input-group-addon "x size"]
         [:span.input-group-addon "4"]
         [:input {:type :range
                  :min 4
                  :max 9
                  :step 1
                  :on-change #(swap! internal-state assoc :x-size (-> % .-target .-value read-string))}]
         [:span.input-group-addon "9"]
         [(fn [] [:span.input-group-addon (:x-size @internal-state)])]]
        [:div.input-group
         [:span.input-group-addon "y size"]
         [:span.input-group-addon "4"]
         [:input {:type :range
                  :min 4
                  :max 9
                  :step 1
                  :on-change #(swap! internal-state assoc :y-size (-> % .-target .-value read-string))}]
         [:span.input-group-addon "9"]
         [(fn [] [:span.input-group-addon (:y-size @internal-state)])]]
        [:div.input-group
         [:span.input-group-addon "neutrals"]
         [:span.input-group-addon "0"]
         [:input {:type :range
                  :min 0
                  :max 11
                  :step 1
                  :on-change #(swap! internal-state assoc :neutral (-> % .-target .-value read-string))}]
         [:span.input-group-addon "11"]
         [(fn [] [:span.input-group-addon (:neutral @internal-state)])]]
        [:div.input-group
         [:span.input-group-addon "players"]
         [:span.input-group-addon "2"]
         [:input {:type :range
                  :min 2
                  :max 5
                  :step 1
                  :on-change #(swap! internal-state assoc :player (-> % .-target .-value read-string))}]
         [:span.input-group-addon "5"]
         [(fn [] [:span.input-group-addon (:player @internal-state)])]]
        [:button.btn.btn-primary
         {:on-click
          #(let [{x :x-size
                  y :y-size
                  n :neutral
                  player :player} @internal-state]
             (init! x y n player))}
         "Init!"]]])))


