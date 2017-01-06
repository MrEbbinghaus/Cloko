(ns cloko.core-test
 (:require
   [cljs.test :refer-macros [is testing]]
   [clojure.test.check :as tc]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop :include-macros true]
   [devcards.core :refer-macros [deftest]]
   [cloko.core]))

(deftest distance-test
  (testing "Planets next to each other are at least 1 distance."
    (is (< 0
           (cloko.core/distance [0 0] [1 0])))))

(deftest how-many-ships-test
         (testing "Basic functionality"
           (is (= 42
                  (cloko.core/how-many-ships [{:ships 25} {:ships 7} {:ships 10}])))))

(deftest fight-vs-fleets-test
         (testing "Single fleet per owner"
           (is (=
                 {:owner :player1
                  :ships 5}
                 (cloko.core/fight-vs-fleets
                   [{:owner :player1 :ships 10}
                    {:owner :player2 :ships 5}
                    {:owner :player3 :ships 3}]))))
         (testing "Multiple fleets per owner"
           (is (=
                 {:owner :player1
                  :ships 5}
                 (cloko.core/fight-vs-fleets
                   [{:owner :player1 :ships 7}
                    {:owner :player2 :ships 5}
                    {:owner :player3 :ships 3}
                    {:owner :player1 :ships 3}])))))

(deftest fight-for-planet-test
         ;; TODO Consider defence bonus
         (testing "Attack on planet"
           (is (=
                 {:owner          :player1
                  :ships          5}
                 (cloko.core/fight-for-planet
                   {:owner          :player1
                    :ships          10}
                   [{:owner :player2 :ships 5}]))))
         (testing "Support for planet"
           (is (=
                 {:owner          :player1
                  :ships          15}
                 (cloko.core/fight-for-planet
                   {:owner          :player1
                    :ships          10}
                   [{:owner :player1 :ships 5}]))))
         (testing "Conquer planet"
           (is (=
                 {:owner          :player2
                  :ships          5}
                 (cloko.core/fight-for-planet
                   {:owner          :player1
                    :ships          10}
                   [{:owner :player2 :ships 15}]))))
         (testing "Do not conquer planet on 0 ships"
           (is (=
                 {:owner          :player1
                  :ships          0}
                 (cloko.core/fight-for-planet
                   {:owner          :player1
                    :ships          10}
                   [{:owner :player2 :ships 10}]))))
         (testing "Fleet battle before conquer of planet"
           (is (=
                 {:owner          :player2
                  :ships          2}
                 (cloko.core/fight-for-planet
                   {:owner          :player1
                    :ships          10}
                   [{:owner :player1 :ships 15}
                    {:owner :player2 :ships 32}
                    {:owner :player3 :ships 20}]))))
         (testing "Empty fleet"
           (is (= {:owner :player1 :ships 5} (cloko.core/fight-for-planet {:owner :player1 :ships 5} [])))))

(deftest fight-test
         (testing "Fight propertys."
           (let [check (tc/quick-check 100 (prop/for-all [a gen/nat
                                                          d gen/nat
                                                          b gen/double]
                                                         (let [result (cloko.core/fight a d b)]
                                                           (and
                                                             (<= (- a) result d)
                                                             (if (= b 0) ((if (< a d) >= <=) result 0) true)))))]
             (print "fight-test smallest: " (get-in check [:shrunk :smallest]))
             (is (:result check)))))

(deftest end-round-test
         (testing "Basic test TODO This is a win situation!"
           (is (=
                 {:world     {
                              :planets {
                                        [1, 1]
                                        {:owner          :player1
                                         :ships-per-turn 4
                                         :ships          14}
                                        [7, 7]
                                        {:owner          :player1
                                         :ships-per-turn 4
                                         :ships          6}}}
                  :movements []
                  :whosTurn 0
                  :round 1
                  :players [:player1 :player2]}
                 (cloko.core/end-round {:world     {
                                                    :planets {
                                                              [1, 1]
                                                              {:owner          :player1
                                                               :ships-per-turn 4
                                                               :ships          10}
                                                              [7, 7]
                                                              {:owner          :player2
                                                               :ships-per-turn 4
                                                               :ships          10}}}

                                        :movements [{:origin [1, 1]
                                                     :target [7, 7]
                                                     :owner :player1
                                                     :ships 20
                                                     :rounds-until-arrival 1}]
                                        :whosTurn 0
                                        :round 0
                                        :players [:player1 :player2]})))))

(deftest arrived-fleets-test
         (testing ""
           (is (= {[1 1] [{:origin [7, 7]
                           :target [1, 1]
                           :owner :player1
                           :ships 10
                           :rounds-until-arrival 0}]}
                  (cloko.core/arrived-fleets [{:origin [7, 7]
                                               :target [1, 1]
                                               :owner :player1
                                               :ships 10
                                               :rounds-until-arrival 0}])))))

(deftest fight-for-planets-test
         (testing "Attack with conquest"
           (is (= {[1, 1] {:owner :player2
                           :ships 4}
                   [7, 7] {:owner :player2
                           :ships 5}}
                  (cloko.core/fight-for-planets {[1, 1]{:owner :player1
                                                        :ships 0}
                                                 [7, 7]{:owner :player2
                                                        :ships 5}}
                                                {[1, 1] [{:target [1, 1]
                                                          :owner :player2
                                                          :ships 4
                                                          :rounds-until-arrival 0}]})))))

(deftest generate-all-ships-test
         (testing "Basic"
           (is (= {[1, 1] {:owner :player1
                           :ships 9
                           :ships-per-turn 4}
                   [7, 7] {:owner :player2
                           :ships 10
                           :ships-per-turn 5}}
                  (cloko.core/generate-all-ships {[1, 1] {:owner :player1
                                                          :ships 5
                                                          :ships-per-turn 4}
                                                  [7, 7] {:owner :player2
                                                          :ships 5
                                                          :ships-per-turn 5}})))))


