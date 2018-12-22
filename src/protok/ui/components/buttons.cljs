(ns protok.ui.components.buttons
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.util :refer [class-names]]
            [clojure.string :as str]
            [protok.styles.colors :refer [colors]]
            [protok.icons :refer [icon]]))

(def button-classes
  [:bw2 :center :overflow-hidden :relative :text-decoration-none])
(def button-styles
  [{:cursor "pointer"
    :outline "none"}
   [:&.type-primary
    {:border-color (colors :blue-3)
     :background-color (colors :blue-3)
     :color (colors :white)}
    [:svg
     ["path:not([fill='none'])" {:fill (colors :white)}]
      [:.stroke {:stroke (colors :white)}]]
    [:&:hover
     {:background (colors :blue-2)
      :border-color (colors :blue-2)}]]
   [:&.type-secondary
    {:border-color (colors :blue-7)
     :background-color "white" 
     :color (colors :blue-4)}
    [:svg
     ["path:not([fill='none'])" {:fill (colors :blue-4)}]
      [:.stroke {:stroke (colors :blue-4)}]]
    [:&:hover
     {:background-color (colors :blue-9)
      :border-color (colors :blue-6)
      :color (colors :blue-3)}
     [:svg
      ["path:not([fill='none'])" {:fill (colors :blue-3)}]
      [:.stroke {:stroke (colors :blue-3)}]]]]
   [:&.type-dangerous
    {:border-color (colors :red-4)
     :background-color (colors :red-4)
     :color (colors :white)}
    [:svg
     ["path:not([fill='none'])" {:fill (colors :white)}]
      [:.stroke {:stroke (colors :white)}]]
    [:&:hover
     {:background (colors :red-3)
      :border-color (colors :red-3)}]]
   [:&.type-link
    {:border-color "transparent" 
     :background-color "transparent" 
     :color (colors :blue-4)}
    [:svg
     ["path:not([fill='none'])" {:fill (colors :blue-4)}]
      [:.stroke {:stroke (colors :blue-4)}]]
    [:&:hover
     {:color (colors :blue-3)
      :text-decoration "underline"}
     [:svg
      ["path:not([fill='none'])" {:fill (colors :blue-3)}]
       [:.stroke {:stroke (colors :blue-3)}]]]]
   [:&.size-big
    {:padding "0 20px"
     :height "44px"
     :line-height "40px"}
    [:&:focus:active
     {:top "1px"
      :transform "scale(0.99)"}]
    [:.protok_ui_components_buttons--icon-wrap
     {:width "36px"
      :top "4px"
      :position "relative"}
     [:&.icon-wrap-left
      {:margin-left "-16px"}]
     [:&.icon-wrap-right
      {:margin-right "-16px"}]
     [:svg
      {:width "22px"
       :height "22px"}]]]
   [:&.size-small
    {:padding "0 15px"
     :height "32px"
     :line-height "28px"}
    [:&:focus:active
     {:top "1px"
      :transform "scale(0.99)"}]
    [:.protok_ui_components_buttons--icon-wrap
     {:position :relative
      :width "28px"
      :top "3px"}
     [:&.icon-wrap-left
      {:margin-left "-14px"}
      [:.icon-email {:position "relative"
                     :left "2px"}]]
     [:&.icon-wrap-right
      {:margin-right "-14px"}
      [:.icon-email {:position "relative"
                     :right "2px"}]]
     [:svg
      {:width "18px"
       :height "18px"}]]]
   [:&:disabled
    {:opacity 0.5}]
   [:&.pill
    [:.protok_ui_components_buttons--icon-wrap
     {:border-radius "999em"}]]
   [:&.rounded
    [:.protok_ui_components_buttons--icon-wrap
     {:border-radius "3px"}]]])

(defelement -button
  :tag :button
  :class button-classes
  :style button-styles)

(defelement -button-link
  :tag :a
  :class button-classes
  :style button-styles)

(defelement -icon-wrap
  :tag :span
  :class [:inline-block :center])

(defn combine-classes [& classes]
  (let [all-classes (map name (filter (complement nil?) (flatten [classes])))]
    (str/join " " all-classes)))

(defn render-icon [side icon-content]
  (when icon-content
    [-icon-wrap
     {:class (class-names {:icon-wrap-left (= :left side)
                           :icon-wrap-right (= :right side)})}
     (if (keyword? icon-content)
       (icon icon-content)
       icon-content)]))

(defn make-button [default-props]
  (fn button
    ([text] (button {} text))
    ([props text]
     (let [default-classes (:class default-props)
           classes         (:class props)
           props'          (merge default-props props)
           button-type     (:button/type props')
           button-size     (:button/size props')
           button-fluid    (:button/fluid props')
           icon-left       (:icon/left props')
           icon-right      (:icon/right props')]
       [(if (:href props')
          -button-link
          -button)
        (-> props' 
            (dissoc :button/type :button/size :button/fluid :button/pill :button/rounded :icon/left :icon/right)
            (assoc :class (class-names {:type-primary (or (nil? button-type) (= :primary button-type))
                                        :type-secondary (= :secondary button-type)
                                        :type-dangerous (= :dangerous button-type)
                                        :type-link (= :link button-type)
                                        :inline-block (not button-fluid)                                      
                                        :pill (:button/pill props')
                                        :rounded (:button/rounded props')
                                        "block w100p" button-fluid
                                        "size-big fs4" (or (nil? button-size) (= :big button-size))
                                        "size-small fs2" (= :small button-size)
                                        (combine-classes default-classes classes) true})))
        [render-icon :left icon-left]
        text
        [render-icon :right icon-right]]))))

(def primary-big (make-button {:button/type :primary :button/size :big}))
(def primary-small (make-button {:button/type :primary :button/size :small}))

(def secondary-big (make-button {:button/type :secondary :button/size :big}))
(def secondary-small (make-button {:button/type :secondary :button/size :small}))

(def dangerous-big (make-button {:button/type :dangerous :button/size :big}))
(def dangerous-small (make-button {:button/type :dangerous :button/size :small}))

(def link-big (make-button {:button/type :link :button/size :big :type :button}))
(def link-small (make-button {:button/type :link :button/size :small :type :button}))
