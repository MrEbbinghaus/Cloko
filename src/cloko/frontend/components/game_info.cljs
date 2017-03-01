(ns cloko.frontend.components.game-info
  (:require [cloko.core :refer [game-state whose-turn]]))

(defn game-info []
  "Returns a react component of a row with informations about the current player, the next player, a possbile winner and the number of the round."
  (fn []
    (let [state @game-state]
      [:div.row
       [:div.col-xs-5
         "Current player: " (whose-turn state) " → Next player: " (whose-turn 1 state)]
       [:div.col-xs-5 (if (= (count (:players state)) 1) (str "Winner: " (first (name (:players state)))))]
       [:div.col-xs-2 "Round: " (:round state)]])))
