(ns protok.ui.components.empty-state
  (:require [protok.icons :refer [icon]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.styles.colors :refer [colors]]))

(defelement -wrap
  :class [:flex :flex-column :items-center :p3])

(defelement -icon-wrap
  :class [:flex :items-center :justify-center :bg-green-8 :mb2]
  :style [{:width "60px"
           :height "60px"
           :border-radius "60px"}
          [:svg
           {:fill (colors :green-3)}]])

(defelement -content-wrap
  :class [:fs2 :c-neutral-4])

(defn render [icon-name content]
  [-wrap
   [-icon-wrap
    (icon icon-name)]
   [-content-wrap
    content]])
