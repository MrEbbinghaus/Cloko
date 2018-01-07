(ns cloko.frontend.components.board
  (:require
    [cloko.core :refer [game-state]]
    [cloko.frontend.utils :refer [fe-state]]))


(def player-colors {:neutral "white"
                    :player1 "red"
                    :player2 "green"
                    :player3 "blue"})
(def planet-icon [:image {:xlink:href "assets/planets/europa.svg"}])
(def nplanet-icon [:image {:xlink:href "assets/planets/mars.svg"}])

(defn set-selected-position!
  "Sets pos as the selected planet in the frontend state."
  [pos]
  (swap! fe-state assoc :selected-planet pos))

(defn planet
  "Returns a react component of a field on the board. Gets two coordinates of the field."
  [[[x y] p]]
  (fn []
    (let [selected? (= (:selected-planet @fe-state) [x y])]
      [:circle.planet
       {:class (str (name (:owner p)) " " (when selected? "selected")) ; theres a bug in reagent
        :cx x :cy y
        :r 0.4
        :data-x x :data-y y
        :on-click #(set-selected-position! [x y])}])))

(defn board []
  "Return a react component of the board."
  (fn []
    (let [world (:world @game-state)
          [x-size y-size] (:size world)]
      [:div.board-wrapper
       [:svg.board
        {:viewBox [-1 -1 (inc x-size) (inc y-size)]}
        (map (comp vector planet) (:planets world))]])))