(ns cloko.frontend.components.send
  (:require [reagent.core :as r]
            [cloko.core :refer [game-state whose-turn distance send!]]
            [cloko.frontend.utils :refer [fe-state position-formater]]
            [cljs.reader :refer [read-string]]))

(defn trow [& elements]
  [:tr (for [e elements]
         ^{:key (rand)} [:td e])])

(defn send-panel []
  (fn []
    (let [state @game-state
          current-player (whose-turn state)
          planet-position (:selected-planet @fe-state)
          planet (get-in state [:world :planets planet-position])
          other-planet-positions (remove #(= % planet-position) (keys (get-in state [:world :planets])))
          internal-state (r/atom {:ships  (:ships planet)
                                  :target (first other-planet-positions)})]
        [:div.panel.panel-default
         [:div.panel-heading "Send fleets"]
         [:div.panel-body
          [:table.table
           [:tbody
            (trow "From" (position-formater planet-position))

            (trow "To" [:select.form-control {:on-change #(swap! internal-state assoc :target (-> % .-target .-value read-string))}
                        (for [p other-planet-positions]
                          ^{:key p} [:option {:value (str p)} (position-formater p)])])

            (trow "Distance" [(fn [] (let [d @internal-state] [:p (distance planet-position (:target @internal-state))]))])

            (trow "How many" [:div.input-group
                              [:span.input-group-addon "0"]
                              [:input {:type :range
                                       :min 0
                                       :max (:ships planet)
                                       :step 1
                                       :on-change #(swap! internal-state assoc-in [:ships] (-> % .-target .-value read-string))}]
                              [:span.input-group-addon (:ships planet)]
                              [(fn [] [:span.input-group-addon (:ships @internal-state)])]])]]

          [:button.btn.btn-success
           {:on-click #(let [fdata @internal-state] (send! planet-position (get-in fdata [:target]) (get-in fdata [:ships])))}
           "Send!"]]])))
