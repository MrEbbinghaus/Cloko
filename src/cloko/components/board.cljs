(ns cloko.components.board
  (:require
    [cloko.core :refer [game-state]]
    [cloko.frontend :refer [fe-state]]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])

(defn set-selected-position!
  [pos]
  (swap! game-state assoc-in [:fe :selected-planet] pos)
  (swap! fe-state assoc :selected-planet pos))

(defn field
  [planet [x y]]
  (if planet
    [:div.field.planet
     {:data-x x :data-y y :on-click #(set-selected-position! [x y])}
     (if (= :neutral (:owner planet)) nplanet-icon planet-icon)]
    [:div.field.space]))

(defn row
  [x y-size planets]
  (mapv #(field (get-in planets [[% x]] nil) [% x]) (range y-size)))

(defn board []
  (fn []
    (let [state @game-state
          [x-size y-size] (get-in state [:world :size])
          planets (get-in state [:world :planets])]
      (vec (apply concat [:div.board] (mapv #(row % y-size planets) (range x-size)))))))
