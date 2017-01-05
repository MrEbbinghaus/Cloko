(ns cloko.core
  (:require [cljs.pprint]
            [cljs.spec]))

(def *distance-factor* 0.5)
(def *defence-bonus* 1.1)
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
  (if (= from to)
    (let [state @game-state
          planets (get-in state [:world :planets])
          origin-planet (get planets from)
          current-player (:whosTurn state)]
      (cond
        (not (player-owns-planet? current-player from planets)) :not-players-planet
        (not (enough-ships-on-planet? origin-planet amount)) :not-enough-ships
        :everything-fine (swap! game-state add-movement from to current-player amount (distance from to))))
    @game-state))

(defn movements! []
  (let [state @game-state
        movements (:movements state)
        current-player (:whosTurn state)
        cols-to-print [:origin :target :ships :turns-until-arrival]]
    (cljs.pprint/print-table cols-to-print (filter #(= current-player (:owner %)) movements))))

(defn- generate-ships-on-planet
  "Updates the ship amount on this planet by adding the ship generation factor."
  [planet]
  (update-in planet [:ships] + (:ships-per-turn planet)))

(defn- arrived-fleets
  "Returns a map of keys of planets and a collection of arriving fleets."
  [movements]
  (->> movements
       (filter #(zero? (:turns-until-arrival %)))
       (group-by :target)))

(defn- how-many-ships
  [fleets]
  (reduce #(+ %1 (:ships %2)) 0 fleets))

(defn- sum-by-owner
  "Gets a collection of fleets and returns a collection where all fleets with common owner are summed."
  [fleets]
  (let [g-fleets (group-by :owner fleets)]
    (map (fn [[k v]] (hash-map :owner k, :ships (how-many-ships v))) g-fleets)))

(defn- fight-vs-fleets
  "Gets a collection of fleets and returns a single fleet of the victor."
  [fleets]
  (let [[f s] (sort-by :ships > (sum-by-owner fleets))]
    (update-in f [:ships] - (:ships s))))

(defn- conquer-planet
  [new-owner planet]
  (assoc-in planet [:owner] new-owner))

(defn- fight
  "Calculates the remaining ships. A negativ number indicates a win of the attacker.
  This"
  ([a d] (- d a))
  ([a d b] (fight a d)))

(defn- fight-vs-planet
  "Gets a planet and an enemy fleet and returns an updated planet"
  [planet fleet]
  (let [battle-result (fight (:ships planet) (:ships fleet))
        conquer (if (< 0 battle-result)
                  (partial conquer-planet (:owner fleet))
                  identity)]
    (conquer (assoc-in planet [:ships] (Math/abs battle-result)))))


(defn- fight-for-planet
  "Gets a planet and a collection of fleets and returns an updated planet which was fought for."
  [planet fleets]
  (let [winner (fight-vs-fleets fleets)]
    (if (= (:owner planet) (:owner winner))
      (update-in planet [:ships] + (:ships winner))
      (fight-vs-planet planet winner))))

