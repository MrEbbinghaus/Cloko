(ns cloko.core)

(def *distance-factor* 0.5)
(def *print-symbols* {:. "."
                      :player1 "1"
                      :player2 "2"
                      :neutral "N"})

(def initial-game-state {:world     {
                                     :size    [9, 9],
                                     :planets {
                                               [1, 1]
                                               {:owner          :player1
                                                :ships-per-turn 4
                                                :ships          10}
                                               [7, 7]
                                               {:owner          :player2
                                                :ships-per-turn 4
                                                :ships          10}

                                               [4, 4]
                                               {:owner :neutral
                                                :ships-per-turn 2
                                                :ships 100}}}

                         :movements [{:origin [1, 1]
                                      :target [8, 8]
                                      :owner :player1
                                      :ships 10
                                      :turns-until-arrival 3}]

                         :whosTurn :player1
                         :turn 0})

(def game-state (atom initial-game-state))

(defn init! [] (reset! game-state initial-game-state))

(defn whose-turn [] (get @game-state :whosTurn))

(defn- place-planets [world planets]
  (reduce #(assoc-in %1 (reverse (get %2 0)) (get-in %2 [1 :owner])) world planets))

(defn- world-as-dots
  "Returns a 2d vector with the dimensions provided by [x y]."
  [[x y]]
  (vec (repeat y (vec (repeat x :.)))))

(defn show-board!
  "Prints the current world. Dots represent empty fields.
  Numbers represent planets of the corresonding player. 'N' belongs to the neutral player"
  []
  (let [world (:world @game-state)
        planets (:planets world)
        dims (:size world)]
    (->> planets
         (place-planets (world-as-dots dims))
         (interpose "\n")
         (flatten)
         (replace *print-symbols*)
         (print))))

(defn players-turn?
  "Returns true if it is players turn."
  [player]
  (= player (whose-turn)))

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
  "Returns true if player owns the planet at position position."
  [player position planets]
  (= player (get-in planets [position :owner])))

(defn- add-movement
  "Returns a movements map with an entry of a new movement. Does not check if there are enugh ships!"
  [old-state from to player amount turns]
  (let [planet (get-in old-state [:world :planets from])]
    (-> old-state
        (update-in [:ships] - amount)
        (update-in [:movements] conj {:origin from
                                      :target to
                                      :owner player
                                      :ships amount
                                      :turns-until-arrival turns}))))

(defn enough-ships-on-planet?
  [planet amount]
  (> (:ships planet) amount))

(defn send!
  [from to amount]
  (let [state @game-state
        planets (get-in state [:world :planets])
        origin-planet (get planets from)
        current-player (:whosTurn state)]
    (cond
      (not (player-owns-planet? current-player from planets)) :not-players-planet
      (not (enough-ships-on-planet? origin-planet amount)) :not-enough-ships
      :everything-fine (swap! game-state add-movement from to current-player amount (distance from to)))))

(defn movements! []
  (let [state @game-state
        movements (:movements state)
        current-player (:whosTurn state)
        cols-to-print [:origin :target :ships :turns-until-arrival]]
    (cljs.pprint/print-table cols-to-print (filter #(= current-player (:owner %)) movements))))

(defn generate-ships
  "Updates the ship amount on this planet by adding the ship generation factor."
  [planet]
  (update-in planet [:ships] + (:ships-per-turn planet)))