(ns cloko.core)

(def *distance-factor* 0.5)

(def initial-game-state {:world     {
                                     :size    [9, 9],
                                     :planets [
                                               {:position       [1, 1]
                                                :owner          :player1
                                                :ships-per-turn 4
                                                :ships          10}

                                               {:position       [7, 7]
                                                :owner          :player2
                                                :ships-per-turn 4
                                                :ships          10}

                                               {:position [4, 4]
                                                :owner :neutral
                                                :ships-per-turn 2
                                                :ships 100}]}

                         :movements [{:origin [1, 1]
                                      :target [8, 8]
                                      :owner :player1
                                      :ships 10
                                      :turns-until-arrival 3}]

                         :whosTurn :player1})

(def game-state (atom initial-game-state))

(defn init! [] (reset! game-state initial-game-state))

(defn whose-turn [] (get @game-state :whosTurn))

(defn- place-planets [world planets]
  (reduce #(assoc-in %1 (reverse (:position %2)) (:owner %2)) world planets))

(defn- world-as-dots
  "Returns a 2d vector with the dimensions provided by [x y]."
  [[x y]]
  (vec (repeat y (vec (repeat x :.)))))

(defn show-board!
  "Prints the current world. Dots represent empty fields.
  Numbers represent planets of the corresonding player. 'N' belongs to the neutral player"
  []
  (let [world (get @game-state :world)
        planets (get world :planets)
        dims (get world :size)]
    (->> planets
         (place-planets (world-as-dots dims))
         (interpose "\n")
         (flatten)
         (replace {:. "." :player1 "1" :player2 "2" :neutral "N"})
         (print))))

(defn players-turn?
  "Returns true if it is players turn."
  [player]
  (= player (whose-turn)))

(defn update-planet
  "Updates the ship amount on this planet by adding the ship generation factor."
  [planet]
  (update-in planet [:ships] + (:ships-per-turn planet)))

(defn- pythagoras
  "Returns the length of the hypotenuse in a right-angle triangle with sides a and b"
  [a b]
  (Math/sqrt (+ (* a a) (* b b))))

(defn distance
  [from to]
  (let [[x1 y1] from
        [x2 y2] to]
    (->> (pythagoras (Math/abs (- x1 x2)) (Math/abs (- y1 y2)))
         (* *distance-factor*)
         (Math/ceil))))

(defn player-owns-planet?
  "Returns true if "
  [player position]
  (let [planets (get-in @game-state [:world :planets])]
      (some #(and
               (= (:position %) position)
               (= (:owner %) player))
            planets)))