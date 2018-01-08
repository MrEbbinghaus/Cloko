(ns cloko.frontend.components.board
  (:require
    [cloko.core :refer [game-state whose-turn distance]]
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


(defn fleet [{:keys [origin target owner ships rounds-until-arrival]}]
  (fn []
    (let [[width height :as ship-dimensions] [0.2 0.2]
          central-position (map #(- %1 (/ %2 2)) origin ship-dimensions)
          o-vector (mapv - target origin)
          time (distance origin target)
          translated (map + central-position
                            (map #(* (/ % time) (- time rounds-until-arrival)) o-vector))
          angle (/ (apply #(Math/atan2 %1 %2) o-vector) (/ Math/PI 180))]
      [:polygon.fleet
        {:points "0.10 0.01 0.01 0.19 0.10 0.15 0.19 0.19"
         :transform (str "translate" translated
                         " rotate" (list (- 0 angle 180) (/ width 2) (/ height 2)))}])))



(defn board []
  "Return a react component of the board."
  (fn []
    (let [state @game-state
          world (:world state)
          [x-size y-size] (:size world)]
      [:div.board-wrapper
       [:svg.board
        {:viewBox [-1 -1 (inc x-size) (inc y-size)]}
        [:g#planets
         (map (comp vector planet) (:planets world))]
        [:g#fleets
         (->> (:movements state)
           (filter #(= (whose-turn state) (:owner %)))
           (map fleet)
           (map vector))]]])))