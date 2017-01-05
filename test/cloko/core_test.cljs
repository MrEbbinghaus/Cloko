(ns cloko.core-test
 (:require
   [cljs.test :refer-macros [is testing]]
   [devcards.core :refer-macros [deftest]]
   [cloko.core]))

(deftest distance-test
  (testing "Planets next to each other are at least 1 distance."
    (is (< 0 (cloko.core/distance [0 0] [1 0])))))