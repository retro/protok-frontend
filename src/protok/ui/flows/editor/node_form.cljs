(ns protok.ui.flows.editor.node-form
  (:require [keechma.ui-component :as ui] 
            [keechma.toolbox.ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]
            [protok.domain.form-ids :as form-ids]
            [keechma.toolbox.ui :refer [route> sub>]]
            [protok.ui.shared :refer [<submit-exclusive]]))

(defelement -form
  :tag :form
  :class [:p3])

(defn flow-node-select [ctx form-props path]
  (let [node-id (:node-id (route> ctx))
        nodes (filter #(not= node-id (:id %)) (sub> ctx :current-flow-nodes))]
    [inputs/select ctx form-props path 
     {:label "Target Node"
      :placeholder "Select Target Node"
      :options (map (fn [o] [(:id o) (:name o)]) nodes)}]))

(defn render-screen-form [ctx form-props]
  [:<>
   [inputs/text ctx form-props :name 
    {:label "Name"
     :placeholder "Name"}]])

(defn render-event-form [ctx form-props]
  [:<>
   [inputs/text ctx form-props :name 
    {:label "Name"
     :placeholder "Name"}]
   [flow-node-select ctx form-props :targetFlowNode.id]])

(defn render-switch-option [ctx form-props idx]
  [:div.bwt1.bd-neutral-8.mt2.pt2
   [inputs/text ctx form-props [:options idx :name]
    {:label (str "Option name (" idx ")")}]
   [flow-node-select ctx form-props [:options idx :targetFlowNode :id]]])

(defn render-switch-form [ctx form-props]
  (let [form-state (forms-ui/form-state> ctx form-props)
        options (or (forms-ui/value-in> ctx form-props :options) [])]
    [:<>
     [inputs/text ctx form-props :name 
      {:label "Name"
       :placeholder "Name"}]
     [:div
      (map-indexed
       (fn [idx o]
         ^{:key idx}
         [render-switch-option ctx form-props idx])
       options)
      [buttons/secondary-small
       {:on-click #(forms-ui/<set-value ctx form-props :options (conj options {}))
        :type :button}
       "Add Option"]]]))
(defn render-flow-ref-form [ctx form-props])

(defn render [ctx]
  (let [route (route> ctx)
        form-type (sub> ctx :current-flow-node-form-type)
        node-id (:node-id route)
        form-props [form-type node-id]
        form-state (forms-ui/form-state> ctx form-props)
        state (get-in form-state [:state :type])
        submitting? (= :submitting state)
        form-renderer (case form-type
                        :flow-screen render-screen-form
                        :flow-event render-event-form
                        :flow-switch render-switch-form
                        :flow-flow-ref render-flow-ref-form
                        nil)]
    (when form-state
      [-form {:on-submit #(<submit-exclusive ctx form-props %)}
       (when form-renderer
         [form-renderer ctx form-props])
       [:div.flex.justify-end
        [buttons/link-small
         {:href (ui/url ctx (dissoc route :node-id))}
         "Cancel"]
        [buttons/primary-small
         {:button/pill true
          :icon/right (if submitting? :spinner :arrow-forward)
          :disabled submitting?}
         "Save"]]])))

(def component
  (ui/constructor {:renderer render
                   :subscription-deps [:current-flow-nodes
                                       :current-flow-node
                                       :current-flow-node-form-type]}))
