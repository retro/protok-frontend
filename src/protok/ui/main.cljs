(ns protok.ui.main
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd]]))

(defn render [ctx]
  [:div
   [(ui/component ctx :editor)]])

(def component
  (ui/constructor {:renderer render
                   :component-deps [:editor]}))
