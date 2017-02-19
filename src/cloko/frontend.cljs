(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])
(def next-turn-btn [:button.btn {:on-click core/end-turn!} "Next turn"])

(defn whose-turn [state] [:span.badge (name (get (:players state) (:whosTurn state)))])

(defn field
  [planet]
  (cond
    (= :neutral planet) [:div.field.planet nplanet-icon]
    (not (nil? planet)) [:div.field.planet planet-icon]
    :else [:div.field.space]))

(defn row
  [x y-size planets]
  (mapv #(field (get-in planets [[% x] :owner] nil)) (range y-size)))

(defn board [state]
  (let [[x-size y-size] (get-in state [:world :size])
        planets (get-in state [:world :planets])]
    (vec (apply concat [:div.board] (mapv #(row % y-size planets) (range x-size))))))



(defn app []
  (let [state @core/game-state]
    [:div
     [:div (whose-turn state)]
     (board state)
     (planet-info-component {:owner :player1})
     [:div next-turn-btn]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))

