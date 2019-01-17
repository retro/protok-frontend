(ns protok.ui.flows.editor.flow-switch
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.entangled.ui :refer [<comp-cmd]]
            [protok.styles.colors :refer [colors]]
            [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [route>]]))

(defelement -wrap
  :class [:fs1 :c-neutral-2 :p1])

(defelement -option-item
  :tag :li
  :class [:relative :pointer]
  :style [{:padding-left "22px"
           :margin-bottom "5px"}
          [:&:hover
           [:.protok_ui_flows_editor_flow_switch--option-idx
            {:background-color (colors :blue-3)}]]])

(defelement -option-idx
  :class [:absolute :bg-neutral-6 :bold :c-white :center :fs0 :left-0]
  :style [{:height "16px"
           :width "16px"
           :border-radius "8px"
           :line-height "16px"
           :top "2px"}])

(defn render [ctx state node]
  (let [options (:options node)
        node-id (:id node)
        route (route> ctx)
        editing? (= "edit" (:subpage route))]
    [-wrap
     [:span.fs2 (:name node)]
     (when (seq options)
       [:ul.pt1
        (map-indexed
         (fn [idx {:keys [id name] :as o}]
           (let [target-node-id (get-in o [:targetFlowNode :id])]
             ^{:key id}
             [-option-item
              {:on-mouse-enter #(<comp-cmd ctx :highlight-edge [node-id target-node-id])
               :on-mouse-leave #(<comp-cmd ctx :highlight-edge nil)
               :on-click (if editing?
                           #(<comp-cmd ctx :center-node target-node-id)
                           #(ui/redirect ctx (assoc route :node-id target-node-id)))}
              [-option-idx (inc idx)]
              name]))
         options)])]))
