(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]))

(.log js/console "Hello World?!")

(def planet-icon [:a {:href ""}
                  [:img {:src "assets/planets/europa.svg"}]])

(defn field
  [planet]
  (if planet
    [:td.planet planet-icon]
    [:td.space]))

(defn row
  [x y-size planets]
  (apply conj [:tr] (mapv #(field (get-in planets [[% x] :owner] nil)) (range y-size))))

(defn board []
  (core/init!)
  (let [state @core/game-state
        [x-size y-size] (get-in state [:world :size])
        planets (get-in state [:world :planets])]
    (apply conj [:table.board] (mapv #(row % y-size planets) (range x-size)))))

(defn movement-info []
  (let [state @core/game-state]))

(defn app []
  (let [state @core/game-state]
    [:div
     [:div (board)]
     [:div (movement-info)]]))

(defn ^:export main []  ;; do not forget the export
  (r/render [app]
            (.getElementById js/document "app")))

