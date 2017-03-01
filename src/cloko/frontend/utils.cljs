(ns cloko.frontend.utils
  "Contains utilities for the frontend."
  (:require [reagent.core :as r]
            [clojure.string :refer [join]]))

;; inter-component state
(defonce fe-state (r/atom {}))

(defn position-formater
  "Gets a vector v and returns a string in the formatation of the game"
  [v] (join ":" v))

