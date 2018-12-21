(ns protok.ui.components.layout
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [<cmd sub> route>]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.util :refer [select-keys-by-namespace gravatar-url]]
            [keechma.toolbox.util :refer [class-names]]
            [protok.svgs :refer [logo-picto]]
            [protok.ui.components.buttons :as button]))

(def header-height "50px")

(def default-props
  {:layout/below-header true})

(defelement -account-wrap
  :class [:flex :flex-row :items-center]
  :style {:line-height "28px"})

(defelement -avatar-img
  :tag :img
  :class [:overflow-hidden :ml2]
  :style {:height "32px"
          :width "32px"
          :border-radius "15px"})

(defn render-account-menu [ctx]
  (let [account (sub> ctx :current-account)]
    [-account-wrap
     [button/link-small {:href (ui/url ctx {:page "settings"})} "Settings"]
     [button/link-small {:button/pill true
                         :on-click #(<cmd ctx [:user-actions :logout])} "Logout"]
     [-avatar-img {:src (gravatar-url (:email account))}]]))

(defelement -header
  :class [:fixed :w100vw :top-0 :left-0 :flex :px2 :flex-row :items-center :justify-between]
  :style {:height header-height
          :background "rgba(255,255,255,0.5)"})

(defelement -logo-wrap
  :style {:width "25px"}
  :class [])


(defn render-header [ctx]
  [-header
   [-logo-wrap
    [logo-picto]]
   [render-account-menu ctx]])

(defn get-props [children]
  (let [f (first children)]
    (if (map? f) f {})))

(defn get-children [children]
  (if (map? (first children))
    (rest children)
    children))

(defelement -wrap)

(defelement -inner-wrap
  :style [[:&.below-header
           {:padding-top header-height}]])

(defn render [ctx & children]
  (let [props (merge default-props (get-props children))]
    [-wrap (select-keys-by-namespace props)
     [render-header ctx]
     (into [-inner-wrap {:class (class-names {:below-header (:layout/below-header props)})}]
           (get-children children))]))

(def component
  (ui/constructor {:renderer render
                   :subscription-deps [:current-account]}))
