(ns cloko.core
  "The core of the cloko game from here you can play the game on the REPL."
  (:require [cljs.pprint :as pprint]
            [cljs.spec.alpha :as s]
            [reagent.core :as reagent]
            [cognitect.transit :as transit]
            [clojure.math.combinatorics :as combo]))


(def *distance-factor* 0.5)
(def *defence-bonus* 1.1)
(def print-symbols {:. "."
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
                                      :target [7, 7]
                                      :owner :player1
                                      :ships 10
                                      :rounds-until-arrival 3}]

                         :whose-turn 0
                         :round     0
                         :players   [:player1 :player2]
                         :log []})

(s/def ::owner keyword?)
(s/def ::ships-per-turn pos-int?)
(s/def ::ships pos-int?)
(s/def ::rounds-until-arrival pos-int?)
(s/def ::planet (s/keys :req-un [::owner ::ships-per-turn ::ships]))
(s/def ::planets (s/map-of ::coordinates ::planet))
(s/def ::coordinates (s/coll-of pos-int? :kind vector? :count 2))
(s/def ::origin ::coordinates)
(s/def ::target ::coordinates)
(s/def ::movement (s/keys :req-un [::origin ::target ::owner ::ships ::rounds-until-arrival]))
(s/def ::movements (s/coll-of ::movement))
(s/def ::size ::coordinates)
(s/def ::world (s/keys :req-un [::size ::planets]))
(s/def ::whose-turn pos-int?)
(s/def ::round pos-int?)
(s/def ::players (s/coll-of ::owner :kind vector?))
(s/def ::log coll?)
(s/def ::state (s/keys :req-un [::world ::movements ::whose-turn ::round ::log]))


(def game-state (reagent/atom initial-game-state))

(defn- random-positions [x y]
  (map vec (shuffle (combo/cartesian-product (range x) (range y)))))

(defn- gen-planet
  ([owner spt] (gen-planet owner spt 0))
  ([owner spt ships] {:owner owner :ships-per-turn spt :ships 0}))

(defn- gen-game-state [size planets players]
  {:world     {
               :size    size,
               :planets planets}
   :movements []
   :whose-turn 0
   :round     0
   :players   players})

(s/fdef gen-game-state
        :args (s/cat :size ::size
                     :planets ::planets
                     :players ::players)
        :ret ::state)

(defn init!
  "If no argument is given, init! will set the game to an example game-state. (Mostly for debugging)
  Otherwise init! takes four arguments.
  First and second: width and height of the world.
  Thrid: amount of random neutral planets (>= 0).
  Fourth: number of human players. (>= 0)
  You are responsible to choose a large enough world size so that there is enoug space for all the planets.
  (A world size of 9x9 is recommended)"
  ([] (reset! game-state initial-game-state))
  ([x y n player]
   (let [positions (random-positions x y)
         players (vec (map #(keyword (str "player" (inc %))) (range player)))
         player-planets (apply merge (for [p (range player)]
                                       {(nth positions p) (gen-planet (keyword (str "player" (inc p))) (+ 4 (rand-int 3)))}))
         neutral-planets (apply merge (for [neutral (range n)]
                                        {(nth (drop player positions) neutral) (gen-planet :neutral (+ 2 (rand-int 6)) (+ 5 (rand-int 35)))}))
         planets (merge player-planets neutral-planets)]
     (reset! game-state (gen-game-state [x y] planets players)))))

(defn whose-turn
  "Gets either none, one or two arguments.
  If none argument is given, the current-player will be returned.
  If one argument is given, the argument has to be a game-state map from which the current player will be returned.
  If two arguments are given, the first argument has to be the position in line of waiting players
  (0 = current player, 1 = next player, ...). The second argument has to be a game-state map"
  ([] (whose-turn @game-state))
  ([state] (whose-turn 0 state))
  ([steps state] (nth (cycle (:players state)) (+ (:whose-turn state) (if (pos? steps) steps 0)))))

(defn- place-planets [world planets]
  (reduce #(assoc-in %1 (reverse (get %2 0)) (get-in %2 [1 :owner])) world planets))

(defn- world-as-dots
  "Returns a 2d vector with the dimensions provided by [x y]."
  [[x y]]
  (vec (repeat y (vec (repeat x :.)))))

(defn- player-owns-planet?
  "Returns true if player owns the planet at position position."
  [player planet]
  (= player (:owner planet)))

(defn- players-planets
  "Returns a map of only the planets which belong to player "
  [player planets]
  (->> (filter (fn [[_ planet]] (player-owns-planet? player planet)) planets)
       (apply concat)
       (apply hash-map)))

(defn show-board!
  "Prints the current world. Dots represent empty fields.
  Numbers represent planets of the corresonding player. 'N' belongs to the neutral player.
  You may want to reset 'print-symbols' if you play with more then two people on the REPL!"
  []
  (let [state @game-state
        planets (get-in state [:world :planets])
        dims (get-in state [:world :size])
        keys-to-print [:position :ships :ships-per-turn]]
    ; Print world grid
    (print (with-out-str (->> planets
                              (place-planets (world-as-dots dims))
                              (interpose "\n")
                              (flatten)
                              (replace print-symbols)
                              (println))
                         ; Print current players planets
                         (->> planets
                              (players-planets (whose-turn state))
                              (map (fn [[pos planet]] (assoc planet :position pos)))
                              (sort-by :position)
                              (pprint/print-table keys-to-print))))))

(defn players-turn?
  "Returns true if it is players turn, false otherwise."
  [player]
  (= player (whose-turn)))

(defn- pythagoras
  "Returns the length of the hypotenuse in a right-angle triangle with sides a and b"
  [a b]
  (Math/sqrt (+ (* a a) (* b b))))

(defn distance
  "Gets to positions of planets as vectors and returns the ingame-distance in rounds between them."
  [from to]
  (let [[x1 y1] from
        [x2 y2] to]
    (->> (pythagoras (Math/abs (- x1 x2)) (Math/abs (- y1 y2)))
         (* *distance-factor*)
         (Math/ceil))))

(defn- player-movements
  ([state]
   (let [movements (:movements state)
         current-player (whose-turn state)]
     (player-movements current-player movements)))
  ([player movements]
   (filter #(= player (:owner %)) movements)))

(defn- add-movement
  "Returns a movements map with an entry of a new movement. Does not check if there are enugh ships!"
  [state from to player amount turns]
  (let [planet (get-in state [:world :planets from])]
    (-> state
        (update-in [:world :planets from :ships] - amount)
        (update-in [:movements] conj {:origin from
                                      :target to
                                      :owner player
                                      :ships amount
                                      :rounds-until-arrival turns}))))

(defn- update-movements
  [movements]
  (map #(update-in % [:rounds-until-arrival] dec) movements))

(defn- clear-movements
  [movements]
  (filter #(pos? (:rounds-until-arrival %)) movements))

(defn- enough-ships-on-planet?
  [planet amount]
  (>= (:ships planet) amount 0))

(defn- print-failure [msg state]
  (print msg)
  state)

(defn send!
  "Sends ships from one planet to another. Gets a vector of the position of the origin planet, a vector of the target
   planet and a positive amount of ships to send.
   Prints 'meaningfull' failure messages if the current player isn't the owner of the origin planet, there are not
   enough ships on the origin planet, the target isn't a planet or the target is equal to the orgin.
   eg.: (send! [0 0] [1 1] 10)

   May get as a fourth argument a game-state map. In this case the function is pure (except for the prints)"
  ([from to amount]
   (swap! game-state #(send! from to amount %)))
  ([from to amount state]
   (if (not= from to)
     (let [planets (get-in state [:world :planets])
           origin-planet (get planets from)
           current-player (whose-turn state)]
       (cond
         (not (get planets to)) (print-failure "No planet to send to!" state)
         (not (player-owns-planet? current-player origin-planet)) (print-failure "You don't own the planet!" state)
         (not (enough-ships-on-planet? origin-planet amount)) (print-failure "You don't have enough ships" state)
         :everything-fine (add-movement state from to current-player amount (distance from to))))
     (print-failure "from = to!" state))))

(defn movements! []
  "Returns all movements of the current player"
  (let [state @game-state
        current-player (whose-turn state)
        movements (:movements state)
        cols-to-print [:origin :target :ships :rounds-until-arrival]]
    (pprint/print-table cols-to-print (player-movements current-player movements))))

(defn- map-vals
  "Applys an f to every value of a map. Returns a new map with same keys."
  [f m]
  (apply hash-map (mapcat (fn [[k v]] [k (f k v)]) m)))

(defn- generate-all-ships
  "Generates ships on all planets"
  [planets]
  (map-vals #(update-in %2 [:ships] + (get %2 :ships-per-turn)) planets))

(defn- arrived-fleets
  "Returns a map of keys of planets and a collection of arriving fleets."
  [movements]
  (->> movements
       (filter #(zero? (:rounds-until-arrival %)))
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
  "Calculates the remaining ships. A negativ number indicates a win of the attacker."
  ([a d] (- d a))
  ([a d b] (let [db (* d b *defence-bonus*)
                 absolute (+ db a)
                 r (- db a)]
             (cond
               (zero? absolute) 0
               (>= r 0) (* d (/ db absolute))
               :else (- (* a (/ a absolute)))))))

(defn- fight-vs-planet
  "Gets a planet and an enemy fleet and returns an updated planet"
  [planet fleet]
  (let [battle-result (fight (:ships fleet) (:ships planet))
        conquer (if (neg? battle-result)
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

(defn- fight-for-planets
  "Returns fought for planets"
  [planets arrived-fleets]
  (map-vals #(fight-for-planet %2 (get arrived-fleets %1 [])) planets))

(defn- get-remaining-players
  [state]
  (-> state
    (#(concat
        (map :owner (:movements %))
        (map (fn [[_ v]] (:owner v)) (get-in % [:world :planets]))))
    (set)
    (disj :neutral)
    (vec)))

(defn- print-victory-message [player]
  (println (name player) " wins!\nYou may end the game now."))

(defn- victory?
  [state]
  (= 1 (count (:players state))))

(defn- set-remaining-players
  [state]
  (assoc-in state [:players] (sort (get-remaining-players state))))

(defn- end-round
  [game-state]
  (-> game-state
      (assoc-in [:whose-turn] 0)
      (update-in [:world :planets] generate-all-ships)
      (update-in [:movements] update-movements)
      (#(update-in % [:world :planets] fight-for-planets (arrived-fleets (:movements %))))
      (update-in [:movements] clear-movements)
      (update-in [:round] inc)
      (set-remaining-players)
      (#(do
          (when (victory? %) (print-victory-message (first (:players %))))
          %))))

(defn- cycle-complete?
  [whose-turn players]
  (>= (inc whose-turn) (count players)))

(defn- end-turn
  ; Complected!
  [state]
  (if (cycle-complete? (:whose-turn state) (:players state))
    (end-round state)
    (update-in state [:whose-turn] inc)))

(defn end-turn! "Ends the turn of the current player" [] (swap! game-state end-turn))

(defn- save [o]
  (let [w (transit/writer :json)]
    (transit/write w o)))

(defn- load [transit]
  (let [r (transit/reader :json)]
    (transit/read r transit)))

(defn save! "Returns the current state as a transit (similar to EDN) string."[]
  (save @game-state))

(defn load! "Gets a transit string and sets the game-state accordingly"
  [input]
  (reset! game-state (load input)))