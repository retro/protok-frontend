(ns protok.ui.flows.editor.node-form-shared
  (:require [keechma.ui-component :as ui] 
            [keechma.toolbox.ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]
            [protok.domain.form-ids :as form-ids]
            [keechma.toolbox.ui :refer [<cmd route> sub>]]
            [protok.ui.shared :refer [<submit-exclusive]]
            [oops.core :refer [oget]]
            [protok.domain.project-files :as project-files]
            [protok.ui.flows.editor.shared :refer [node-type-name]]
            [protok.icons :refer [icon]]
            [protok.styles.colors :refer [colors]]))

(defelement -form-subtitle
  :class [:fs3 :c-neutral-2 :mb1])

(defn node->option [o]
  {:value (:id o) :label (:name o)})

(defn flow-ref->option [f]
  (when-let [flow (:targetFlow f)]
    {:value (:id f) :label (:name flow)}))

(defn flow-node-select
  ([ctx form-props path] (flow-node-select ctx form-props path {}))
  ([ctx form-props path options]
   (let [node-id (:node-id (route> ctx))
         nodes (filter #(not= node-id (:id %)) (sub> ctx :current-flow-nodes))
         grouped-nodes (group-by :type nodes)
         optgroups [{:label "Screens"
                     :options (map node->option (grouped-nodes "SCREEN"))}
                    {:label "Events"
                     :options (map node->option (grouped-nodes "EVENT"))}
                    {:label "Switches"
                     :options (map node->option (grouped-nodes "SWITCH"))}
                    {:label "Flow Refs"
                     :options (filter (complement nil?) (map flow-ref->option (grouped-nodes "FLOW_REF")))}]]
     [inputs/select ctx form-props path 
      (merge
       {:label "Target Node"
        :placeholder "Select Target Node"
        :optgroups optgroups
        :input/size :normal}
       options)])))

(defelement -hotspot-options-wrap
  :class [:relative :bwb1 :bd-neutral-8 :mb2]
  :style [{:padding-left "30px"}
          [:&:last-child
           {:border "none"
            :margin-bottom 0}]])

(defelement -hotspot-options-index
  :class [:absolute :left-0 :c-white :bg-neutral-6 :center :bold]
  :style [{:height "20px"
           :width "20px"
           :top "1px"
           :border-radius "10px"
           :line-height "18px"}])

(defelement -hotspot-options-inner-wrap
  :style [{:display "grid"
           :grid-auto-columns "1fr 1fr 24px"
           :grid-auto-flow "column"
           :grid-column-gap "1rem"}])

(defelement -remove-button-wrap
  :class [:flex :items-end :pb2])

(defelement -remove-button
  :tag :button
  :class [:flex :justify-center :items-center :p0 :pointer]
  :style [{:height "30px"
           :width "24px"
           :outline "none"
           :border "none"
           :background "transparent"}
          [:svg {:fill (colors :neutral-6)}]
          [:&:hover [:svg {:fill (colors :red-4)}]]])

(defn render-hotspots-options [ctx form-props child-collection-type idx]
  (let [remove-action (case child-collection-type
                        :hotspots :remove-hotspot
                        :options :remove-option
                        nil)]
    [-hotspot-options-wrap
     [-hotspot-options-index (inc idx)]
     [-hotspot-options-inner-wrap
      [inputs/text ctx form-props [child-collection-type idx :name]
       {:label "Name"
        :input/size :small}] 
      [flow-node-select ctx form-props [child-collection-type idx :targetFlowNode :id] {:input/size :small}]
      [-remove-button-wrap
       [-remove-button
        {:type :button
         :on-click (when remove-action #(<cmd ctx [:flow-editor remove-action] {:form-props form-props :idx idx}))}
        (icon :remove-circle-outline)]]]]))
