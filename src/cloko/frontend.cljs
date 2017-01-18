(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]))

(.log js/console "Hello World?!")

(def planet-icon [:img {:src "assets/planets/europa.svg"}])

(defn field
  [planet]
  (if planet
    [:div.field.planet planet-icon]
    [:div.field.space]))

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
  (core/init!)
  (r/render [app]
            (.getElementById js/document "app")))

