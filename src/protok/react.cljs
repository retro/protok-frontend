(ns protok.react
  (:require [reagent.core :as r]
            [oops.core :refer [oget]]
            [react-resize-detector]
            [react-svg-pathline]))

(def resize-detector (r/adapt-react-class react-resize-detector))
(def pathline (r/adapt-react-class react-svg-pathline))
