(ns protok.ui.flows.editor.flow-flow-ref
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.ui-component :as ui]))

(defelement -wrap
  :class [:fs2 :c-neutral-2 :p1])

(defn render [ctx state node]
  [-wrap
   (if-let [flow (:targetFlow node)]
     [:a.c-blue-3 {:href (ui/url ctx {:page "flows" :subpage "edit" :id (:id flow)})} (:name flow)]
     "Flow is not selected")])
