(ns protok.react
  (:require [reagent.core :as r]
            [oops.core :refer [oget]]
            [react-resize-detector]))

(def resize-detector (r/adapt-react-class react-resize-detector))
