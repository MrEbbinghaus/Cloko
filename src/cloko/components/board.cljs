(ns cloko.components.board
  (:require
    [cloko.core :as core]
    [cloko.frontend :as fe]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])

(defn set-selected-position!
  [pos]
  (swap! core/game-state assoc-in [:fe :selected-planet] pos))

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

(defn board [state]
  (let [[x-size y-size] (get-in state [:world :size])
        planets (get-in state [:world :planets])]
    (vec (apply concat [:div.board] (mapv #(row % y-size planets) (range x-size))))))