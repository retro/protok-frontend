(ns protok.ui.flows.editor
  (:require [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd]]
            [protok.ui.editor.actions :refer [actions]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [reagent.core :as r]
            [protok.ui.flows.editor.actions :refer [actions]]
            [keechma.toolbox.ui :refer [sub>]]
            [protok.ui.components.empty-state :as empty-state]
            [protok.icons :refer [icon]]
            [protok.styles.colors :refer [colors]]
            [protok.ui.flows.editor.main :as main]))

(def flow-node-types
  [{:type :flow-screen
    :name "Screen"}
   {:type :flow-event
    :name "Event"}
   {:type :flow-switch
    :name "Switch"}
   {:type :flow-flow-ref
    :name "Flow Ref"}])

(defelement -empty-state-wrap
  :class [:w100p :h100p :flex :items-center :justify-center])

(defelement -empty-state-inner-wrap
  :class [:bg-white :sh3 :rounded]
  :style {:width "500px"})

(defelement -empty-state-buttons-wrap
  :class [:flex :justify-between :w100p :mt3 :mb1])

(defelement -empty-state-button
  :tag :button
  :class [:rounded :bd-blue-8 :bg-h-blue-8 :bw1 :block :p1 :bg-blue-9 :c-blue-1 :fs1 :pointer]
  :style [{:width "23%"
           :outline "none"}])

(defelement -empty-state-icon-wrap
  :tag :span
  :class [:bg-white :flex :justify-center :items-center :bg-blue-3 :my1 :mx-auto]
  :style [{:width "40px"
           :height "40px"
           :border-radius "20px"}
          [:svg {:display "block"
                 :fill (colors :white)}]])

(defn render-empty-state [ctx state]
  [-empty-state-wrap
   [-empty-state-inner-wrap
    [empty-state/render
     :waves
     [:div
      [:div.center "This flow is empty. Start by adding a node."]
      [-empty-state-buttons-wrap
       (map 
        (fn [n]
          (let [node-type (:type n)]
            ^{:key node-type}
            [-empty-state-button
             {:on-click #(<comp-cmd ctx :create-node [node-type true])}
             [-empty-state-icon-wrap
              (icon (:type n))]
             "Add " (:name n)]))
        flow-node-types)]]]]])

(defelement -wrap
  :class [:w100p :h100p :overflow-hidden])

(defn render [ctx state]
    (when (:initialized? state)
      (let [nodes-getter (get-in state [:flow :flowNodes])]
        (if (seq (nodes-getter))
          [main/render ctx state]
          [render-empty-state ctx state]))))

(defn state-provider [ctx local-state args]
  (-> local-state
      (assoc :flow (sub> ctx :current-flow))))

(def component
  (-> (entangled-ui/constructor
       {:renderer render
        :state-provider state-provider
        :subscription-deps [:current-flow]
        :component-deps [:flows/node-form]}
       actions)
      (assoc :protok/config {:layout :bare})))
