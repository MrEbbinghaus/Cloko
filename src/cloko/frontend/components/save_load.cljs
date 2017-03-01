(ns cloko.frontend.components.save-load
  (:require [cloko.core :refer [game-state save load!]]
            [reagent.core :refer [atom]]))

(defn save-component []
  (fn []
    [:div.panel.panel-default
     [:div.panel-heading "Save"]
     [:div.panel-body
      [:input.form-control {:type "text"
                            :readOnly true
                            :value (save @game-state)}]]]))

(defn load-component []
  (fn []
    (let [internal-state (atom "")]
      [:div.panel.panel-default
       [:div.panel-heading "Load"]
       [:div.panel-body
        [:div.input-group
         [:input.form-control {:type "text" :on-change #(reset! internal-state (-> % .-target .-value))}]
         [:span.input-group-btn [:button.btn..btn-primary {:on-click #(load! @internal-state)} "Load!"]]]]])))
