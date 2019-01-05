(ns protok.ui.flows.editor.shared
  (:require [protok.icons :refer [icon]]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]]))

(def flow-node-types
  [{:type :flow-screen
    :name "Screen"}
   {:type :flow-event
    :name "Event"}
   {:type :flow-switch
    :name "Switch"}
   {:type :flow-flow-ref
    :name "Flow Ref"}])

(defn node-type->keyword [node-type]
  (->kebab-case-keyword (str "flow-" node-type)))

(defn node-type-icon [node-type]
  (icon (node-type->keyword node-type)))

(defn node-type-name [node-type]
  (let [node-type-keyword (node-type->keyword node-type)]
    (->> flow-node-types
         (filter #(= (:type %) node-type-keyword))
         first
         :name)))
