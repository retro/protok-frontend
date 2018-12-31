(ns protok.ui.not-found
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.buttons :as button]
            [protok.svgs :refer [logo-picto]]
            [oops.core :refer [ocall]]
            [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -not-found-wrap
  :class [:h100vh :w100vw :flex :items-center :justify-center])

(defelement -not-found-inner-wrap
  :class [:rounded :bg-white :sh1 :flex :flex-column :items-center :justify-center :p3]
  :style {:width "300px"})

(defelement -not-found-logo-wrap
  :style {:width "30px"})

(defelement -not-found-copy
  :class [:py2 :c-neutral-4 :fs1 :center])

(defn render [ctx]
  [-not-found-wrap
   [-not-found-inner-wrap
    [-not-found-logo-wrap
     [logo-picto]]
    [-not-found-copy
     "We couldn't find the page"
     [:br]
     "you're looking for."]
    [button/secondary-small
     {:button/pill true
      :icon/left :arrow-back
      :on-click #(ocall js/window :history.back)}
     "Go Back"]]])

(def component
 (ui/constructor {:renderer render}))
