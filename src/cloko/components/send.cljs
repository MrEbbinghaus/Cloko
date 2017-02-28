(ns cloko.components.send
  (:require [reagent.core :as r]
            [cloko.core :as core]))

(defonce data (r/atom {:ships 0}))

(defn send [& es]
  (doseq [e es]
    (.log js/console e))
  true)

(defn trow [& elements]
  (fn [] [:tr (for [e elements]
                ^{:key (rand)} [:td e])]))

(defn slider-component [min max step]
  (fn []
    [:div [:input {:type :range
                   :min min
                   :max max
                   :step step
                   :value (:ships @data)
                   :on-change #(swap! data assoc-in [:ships] (-> % .-target .-value cljs.reader/read-string))}]
     [:p (:ships @data)]]))

(defn select-component [positions]
  (fn []
    [:select.form-control {:on-change #(swap! data assoc-in [:target] (-> % .-target .-value cljs.reader/read-string))}
     (for [p positions]
       ^{:key p} [:option {:value (str p)} (str p)])]))


(defn send-panel [state]
  (fn []
    (let [current-player (core/whose-turn state)
          planet-position (get-in state [:fe :selected-planet])
          planet (get-in state [:world :planets planet-position])
          p-positions (remove #(= % planet-position) (keys (get-in state [:world :planets])))]
        [:div.panel.panel-default
         [:div.panel-heading "Send fleets"]
         [:div.panel-body.input-group
          [:table.table
           [:tbody
            [trow "From" (str planet-position)]
            [trow "To" [select-component p-positions]]
            [trow "How many" [slider-component 0 (:ships planet) 1]]]]

          [:button.btn.btn-success
           {:on-click #(let [fdata @data] (core/send! planet-position (get-in fdata [:target]) (get-in fdata [:ships])))}
           "Send!"]]])))
