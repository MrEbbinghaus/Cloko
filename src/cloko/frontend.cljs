(ns cloko.frontend
  (:require [cloko.core :as core]
            [reagent.core :as r]
            [cloko.components.planet-info :as planet-info]))

(def planet-icon [:img {:src "assets/planets/europa.svg"}])
(def nplanet-icon [:img {:src "assets/planets/mars.svg"}])
(def next-turn-btn [:button.btn {:on-click core/end-turn!} "Next turn"])

(defn whose-turn [state] [:span.badge (name (get (:players state) (:whosTurn state)))])

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

(defn app []
  (let [state @core/game-state]
    [:div
     [:div (whose-turn state)]
     (board state)
     (planet-info/planet-info state)
     [:div next-turn-btn]]))

(defn ^:export main []  ;; do not forget the export
  (core/init! 9 9 3 2)
  (r/render [app]
            (.getElementById js/document "app")))

