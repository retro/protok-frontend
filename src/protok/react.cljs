(ns protok.react
  (:require [reagent.core :as r]
            [oops.core :refer [oget]]
            [react-resize-detector]
            [react-svg-pathline]
            [react-rnd]
            [react-markdown]))

(def resize-detector (r/adapt-react-class react-resize-detector))
(def pathline (r/adapt-react-class react-svg-pathline))
(def rnd (r/adapt-react-class react-rnd))
(def markdown (r/adapt-react-class react-markdown))
