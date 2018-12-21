(ns protok.icons
  (:require [keechma.toolbox.css.core :refer-macros [defelement]])
  (:require-macros [protok.icons :refer [inline-icon]]
                   [garden.def :refer [defkeyframes]]))

(def spinner-config
  {:offset 187
   :duration "1.4s"})

(defkeyframes spinner-dash
  [:0% {:stroke-dashoffset (:offset spinner-config)}]
  [:50% {:stroke-dashoffset (/ (:offset spinner-config) 4)
         :transform "rotate(135deg)"}]
  [:100% {:stroke-dashoffset (:offset spinner-config)
          :transform "rotate(450deg)"}])

(defkeyframes spinner-rotator
  [:0% {:transform "rotate(0deg)"}]
  [:100% {:transform "rotate(270deg)"}])

(defelement -spinner
  :tag :svg
  :style [{:animation [[spinner-rotator (:duration spinner-config) :linear :infinite]]}
          [:circle {:stroke-dasharray (:offset spinner-config)
                    :stroke-dashoffset 0
                    :transform-origin "center"
                    :animation [[spinner-dash (:duration spinner-config) :ease-in-out :infinite]]}]])

(def spinner
  [-spinner {:viewBox "0 0 66 66"}
   [:circle.stroke
    {:fill "none"
     :stroke-width 6
     :stroke-linecap "round"
     :cx "33"
     :cy "33"
     :r "30"}]])

(defn add-icon-classes [icons]
  (reduce-kv
   (fn [m k v]
     (let [has-props? (map? (second v))
           el (first v)
           props (if has-props? (second v) {})
           v' (vec (drop (if has-props? 2 1) v))]
       ;; Maybe merge classes here if needed?
       (assoc m k (into [el (assoc props :class [:icon (str "icon-" (name k))])] v'))))
   {} icons))

(def icons
  (-> {:email         (inline-icon :email)
       :arrow-forward (inline-icon :arrow-forward)
       :arrow-back    (inline-icon :arrow-back)
       :spinner       spinner}
       (add-icon-classes)))

(defn icon [i]
  (let [val (icons i)]
    (if val
      val
      (js/console.error (str "Missing icon " i)))))
