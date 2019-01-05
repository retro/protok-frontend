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
            [keechma.toolbox.ui :refer [<cmd route> sub>]]
            [protok.ui.shared :refer [<submit-exclusive]]
            [oops.core :refer [oget]]
            [protok.domain.project-files :as project-files]
            [protok.ui.flows.editor.shared :refer [node-type-name]]))

(defelement -form
  :tag :form
  :class [:p2])

(defelement -form-title
  :class [:fs4 :c-neutral-2 :mb2])

(defelement -form-subtitle
  :class [:fs3 :c-neutral-2 :mb1])

(defn node->option [o]
  {:value (:id o) :label (:name o)})

(defn flow-node-select [ctx form-props path]
  (let [node-id (:node-id (route> ctx))
        nodes (filter #(not= node-id (:id %)) (sub> ctx :current-flow-nodes))
        grouped-nodes (group-by :type nodes)
        optgroups [{:label "Screens"
                    :options (map node->option (grouped-nodes "SCREEN"))}
                   {:label "Events"
                    :options (map node->option (grouped-nodes "EVENT"))}
                   {:label "Switches"
                    :options (map node->option (grouped-nodes "SWITCh"))}
                   {:label "Flow Refs"
                    :options (map node->option (grouped-nodes "FLOW_REF"))}]]
    [inputs/select ctx form-props path 
     {:label "Target Node"
      :placeholder "Select Target Node"
      :optgroups optgroups
      :input/size :small}]))

(defelement -hotspot-wrap
  :class [:relative :bwb1 :bd-neutral-8 :mb2]
  :style [{:padding-left "30px"}
          [:&:last-child
           {:border "none"
            :margin-bottom 0}]])

(defelement -hotspot-index
  :class [:absolute :left-0 :c-white :bg-neutral-6 :center :bold]
  :style [{:height "20px"
           :width "20px"
           :top "1px"
           :border-radius "10px"
           :line-height "18px"}])

(defelement -dimensions-coordinates-wrap
  :class [:flex :flex-row :justify-between])

(defelement -dimension-coordinate-wrap
  :style [{:width "23.5%"}])

(defn render-screen-hotspot [ctx form-props idx]
  [-hotspot-wrap
   [-hotspot-index (inc idx)]
   [:div
    [inputs/text ctx form-props [:hotspots idx :name]
     {:label "Name"
      :input/size :small}]
    [-dimensions-coordinates-wrap
     [-dimension-coordinate-wrap
      [inputs/text ctx form-props [:hotspots idx :coordinates :top]
       {:label "Top"
        :input/size :small}]]
     [-dimension-coordinate-wrap
      [inputs/text ctx form-props [:hotspots idx :coordinates :left]
       {:label "Left"
        :input/size :small}]]
     [-dimension-coordinate-wrap
      [inputs/text ctx form-props [:hotspots idx :dimensions :width]
       {:label "Width"
        :input/size :small}]]
     [-dimension-coordinate-wrap
      [inputs/text ctx form-props [:hotspots idx :dimensions :height]
       {:label "Height"
        :input/size :small}]]]
    [flow-node-select ctx form-props [:hotspots idx :targetFlowNode :id]]]])

(defelement -screen-img
  :tag :img
  :class [:mx-auto :block :mt1]
  :style [{:max-width "150px"}])

(defn render-screen-form [ctx form-props]
  (let [form-state (forms-ui/form-state> ctx form-props)
        hotspots (or (forms-ui/value-in> ctx form-props :hotspots) [])
        project-file (forms-ui/value-in> ctx form-props :projectFile)]
    [:<>
     [inputs/text ctx form-props :name 
      {:label "Name"
       :placeholder "Name"}]
     [:div.pb2
      [:input
       {:type :file 
        :on-change #(<cmd ctx [:image-uploader :upload] {:file (oget % :target.files.0) :form-props form-props :path [:projectFile]})}]
      (when project-file
        [-screen-img {:src (project-files/url project-file)}])]
     [:div.bwt1.bd-neutral-7.mt2.pt2
      [-form-subtitle "Hotspots"]
      [:div
       (map-indexed
        (fn [idx o]
          ^{:key idx}
          [render-screen-hotspot ctx form-props idx])
        hotspots)]
      [:div.flex.justify-end
       [buttons/secondary-small
        {:on-click #(forms-ui/<set-value ctx form-props :hotspots (conj hotspots {}))
         :type :button
         :button/pill true}
        "Add Hotspot"]]]]))

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
        :type :button
        :button/pill true}
       "Add Option"]]]))
(defn render-flow-ref-form [ctx form-props])

(defn render [ctx]
  (let [route (route> ctx)
        current-flow-node (sub> ctx :current-flow-node)
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
       [-form-title "Edit " (node-type-name (:type current-flow-node))]
       (when form-renderer
         [form-renderer ctx form-props])
       [:div.flex.justify-between.mt2.pt2.bwt1.bd-neutral-7
        [buttons/dangerous-secondary-small
         {:on-click #(<cmd ctx [:flow-editor :delete-node] (:id current-flow-node))
          :button/pill true
          :type :button}
         "Delete"]
        [:div.flex.items-center
         [buttons/link-small
          {:href (ui/url ctx (dissoc route :node-id))}
          "Cancel"]
         [buttons/primary-small
          {:button/pill true
           :icon/right (if submitting? :spinner :arrow-forward)
           :disabled submitting?}
          "Save"]]]])))

(def component
  (ui/constructor {:renderer render
                   :subscription-deps [:current-flow-nodes
                                       :current-flow-node
                                       :current-flow-node-form-type]}))
