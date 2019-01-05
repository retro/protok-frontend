(ns protok.ui.flows.editor.flow-event
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -wrap
  :class [:fs2 :c-neutral-2 :p1])

(defn render [ctx state node]
  [-wrap
   (:name node)])
