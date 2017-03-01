(ns cloko.frontend.components.board
  (:require
    [cloko.core :refer [game-state]]
    [cloko.frontend.utils :refer [fe-state]]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])

(defn set-selected-position!
  "Sets pos as the selected planet in the frontend state."
  [pos]
  (swap! fe-state assoc :selected-planet pos))

(defn field
  "Returns a react component of a field on the board. Gets two coordinates of the field."
  [x y]
  (fn []
    (let [world (:world @game-state)
          planet (get-in world [:planets [x y]] nil)
          [width height] (mapv #(/ 100 %) (:size world))]
      (if planet
        [:div.field.planet
         {:style {:width (str width "%")
                  :height (str height "%")}
          :data-x x :data-y y
          :on-click #(set-selected-position! [x y])}
         (if (= :neutral (:owner planet)) nplanet-icon planet-icon)]
        [:div.field.space {:style {:width (str width "%")
                                   :height (str height "%")}}]))))

(defn board []
  "Return a react component of the board."
  (fn []
    (let [world (:world @game-state)
          [x-size y-size] (:size world)
          planets (:planets world)]
      [:div.outer-board
       [:div.inner-board
        {:style {:width (* 55 x-size)
                 :height (* 55 y-size)}}

        (for [x (range x-size) y (range y-size)]
          ^{:key [x y]} [field x y])]])))