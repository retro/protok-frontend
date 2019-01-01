(ns protok.ui.flows.editor
  (:require [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd]]
            [protok.ui.editor.actions :refer [actions]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [reagent.core :as r]
            [protok.ui.editor.states.shared :refer [selected-multiple?]]))

(defelement -wrap
  :class [:w100p :h100p]
  :style {:overflow-x "hidden"
          :overflow-y "auto"})

(defn render [ctx]
  [-wrap])

(defn state-provider [ctx local-state args])

(def component
  (-> (entangled-ui/constructor
       {:renderer render
        :state-provider state-provider})
      (assoc :protok/config {:layout :bare})))
