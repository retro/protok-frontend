(ns protok.ui.components.layout
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [<cmd sub> route>]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.util :refer [select-keys-by-namespace gravatar-url]]
            [keechma.toolbox.util :refer [class-names]]
            [protok.svgs :refer [logo-picto]]
            [protok.ui.components.buttons :as button]
            [protok.controllers.kv :refer [<kv-reset]]
            [protok.styles.colors :refer [colors]]))

(def header-height "50px")

(def default-props
  {:layout/below-header true})

(defelement -account-wrap
  :class [:flex :flex-row :items-center]
  :style {:line-height "28px"})

(defelement -account-dropmenu-wrap
  :class [:relative :ml2]
  :style {:outline "none"})

(defelement -avatar-img
  :tag :img
  :class [:overflow-hidden :block]
  :style {:height "32px"
          :width "32px"
          :border-radius "15px"})

(defelement -account-dropmenu
  :class [:p2 :absolute :bg-white :sh4 :right-0 :rounded :bd-neutral-7 :bw2 :relative]
  :style [{:width "200px"
           :margin-top "6px"
           :margin-right "-0.5rem"}
          [:&:before
           {:content "''"
            :width 0
            :height 0
            :display "block"
            :border-style "solid"
            :border-width "0 6px 6px 6px"
            :border-color (str "transparent transparent " (colors :neutral-7) " transparent")
            :position "absolute"
            :top "-7px"
            :right "16px"}]
          [:&:after
           {:content "''"
            :width 0
            :height 0
            :display "block"
            :border-style "solid"
            :border-width "0 6px 6px 6px"
            :border-color (str "transparent transparent " (colors :white) " transparent")
            :position "absolute"
            :top "-4px"
            :right "16px"}]])

(defelement -account-dropmenu-user-info-wrap
  :class [:flex :items-center :flex-column :pb2 :mb1 :bdb-neutral-8 :bwb2 :c-neutral-4 :fs1]
  :style [[:.protok_ui_components_layout--avatar-img
           {:width "64px"
            :height "64px"
            :border-radius "32px"}]])

(defn render-account-menu [ctx]
  (let [account (sub> ctx :current-account)
        account-menu-open? (sub> ctx :account-menu-open?)
        email (:email account)
        username (:username account)
        avatar-url (gravatar-url email)]
    [-account-wrap
     [-account-dropmenu-wrap
      {:tab-index 1
       :on-focus #(<kv-reset ctx :account-menu-open? true)
       :on-blur #(<kv-reset ctx :account-menu-open? false)}
      [-avatar-img {:src avatar-url :class "pointer"}]
      (when account-menu-open?
        [-account-dropmenu
         [-account-dropmenu-user-info-wrap
          [-avatar-img {:src avatar-url
                        :class [:mb1]}]
          username
          (when (not= username email)
            [:div.fs0.c-neutral-5 "(" email ")"])]
         [button/link-small
          {:button/fluid true
           :href (ui/url ctx {:page "settings"})
           :class [:mb1]} "Settings"]
         [button/secondary-small
          {:button/pill true
           :button/fluid true
           :on-click #(<cmd ctx [:user-actions :logout])} "Logout"]])]]))

(defelement -header
  :class [:fixed :w100vw :top-0 :left-0 :flex :px2 :flex-row :items-center :justify-between]
  :style {:height header-height
          :background "rgba(255,255,255,0.5)"})

(defelement -logo-wrap
  :style {:width "32px"
          :padding-right "7px"}
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
                   :subscription-deps [:current-account
                                       :account-menu-open?]}))
