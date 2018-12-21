(ns protok.ui.organizations
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -wrap)

(defn render [ctx]
  [(ui/component ctx :component/layout)
   [-wrap "ORGANIZATIONS"]])

(def component
  (ui/constructor {:renderer render
                   :component-deps [:component/layout]}))
