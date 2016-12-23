(ns cloko.core)

(def initial-game-state {:world {
                                   :size [10, 10],
                                   :planets #{
                                              {:position [1, 1]
                                               :owner :player1
                                               :ships-per-turn 4
                                               :ships 10}
                                              {:position [8, 8]
                                               :owner :player2
                                               :ships-per-turn 4
                                               :ships 10}}},
                           :movements [],
                           :whosTurn :player1,})

(def game-state (atom initial-game-state))

(defn init! [] (reset! game-state initial-game-state))

(defn whose-turn [] (get @game-state :whosTurn))

(defn show-board! []
  (let [board (get @game-state :world)
        [size-x size-y] (get board :size)]
    (->> (repeat (* size-x size-y) ".")
         (partition size-x)
         (interpose "\n")
         (flatten)
         )))