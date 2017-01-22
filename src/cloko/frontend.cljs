(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])

(defn field
  [planet]
  (cond
    (= :neutral planet) [:div.field.planet nplanet-icon]
    (not (nil? planet)) [:div.field.planet planet-icon]
    :else [:div.field.space]))

(defn row
  [x y-size planets]
  (mapv #(field (get-in planets [[% x] :owner] nil)) (range y-size)))

(defn board []
  (let [state @core/game-state
        [x-size y-size] (get-in state [:world :size])
        planets (get-in state [:world :planets])]
    (vec (apply concat [:div.board] (mapv #(row % y-size planets) (range x-size))))))

(defn movement-info []
  (let [state @core/game-state]))

(defn app []
  (let [state @core/game-state]
    [:div
     [:div (board)]
     [:div (movement-info)]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))

