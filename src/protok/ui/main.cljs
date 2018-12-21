(ns protok.ui.main
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd route>]]
            [protok.svgs :refer [logo-picto]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.buttons :as button]
            [oops.core :refer [ocall]]))

(defelement -not-found-wrap
  :class [:h100vh :w100vw :flex :items-center :justify-center])

(defelement -not-found-inner-wrap
  :class [:rounded :bg-white :sh1 :flex :flex-column :items-center :justify-center :p3]
  :style {:width "300px"})

(defelement -not-found-logo-wrap
  :style {:width "30px"})

(defelement -not-found-copy
  :class [:py2 :c-neutral-4 :fs1 :center])

(defn render-404 []
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

(defn render [ctx]
  (let [page (:page (route> ctx))]
    [:div
     (case page
       "loading" [(ui/component ctx :loading)]
       "login" [(ui/component ctx :login)]
       "organizations" [(ui/component ctx :organizations)]
       
       [render-404])]))

(def component
  (ui/constructor
   {:renderer render
    :component-deps [:loading
                     :login
                     :organizations]}))
