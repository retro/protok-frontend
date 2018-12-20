(ns protok.ui.main
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd]]
            [protok.svgs :refer [logo-picto logo-mono logo]]))

(defn render [ctx]
  [:div
   [(ui/component ctx :login)]])

(def component
  (ui/constructor {:renderer render
                   :component-deps [:login]}))
