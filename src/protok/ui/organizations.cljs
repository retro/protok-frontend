(ns protok.ui.organizations
  (:require [keechma.ui-component :as ui]))

(defn render [ctx]
  [:div "ORGANIZATIONS"])

(def component
  (ui/constructor {:renderer render}))
