(ns cloko.frontend.utils
  (:require [reagent.core :as r]
            [clojure.string :refer [join]]))

(defonce fe-state (r/atom {}))

(defn position-formater [v] (join ":" v))

