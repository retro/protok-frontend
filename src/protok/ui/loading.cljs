(ns protok.ui.loading
  (:require [keechma.ui-component :as ui]))

(defn render [ctx])

(def component
  (ui/constructor {:renderer render}))
