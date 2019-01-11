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
            [protok.ui.flows.editor.node-form-flow-event]
            [protok.ui.flows.editor.node-form-flow-screen]
            [protok.ui.flows.editor.node-form-flow-switch]
            [protok.ui.flows.editor.node-form-flow-flow-ref]
            [protok.ui.flows.editor.shared :refer [node-type-name]]
            [protok.icons :refer [icon]]
            [protok.styles.colors :refer [colors]]))

(defelement -form
  :tag :form
  :class [:px2 :pt2])

(defelement -form-title
  :class [:fs4 :c-neutral-2 :mb2])

(defelement -form-title-wrap
  :class [:flex :justify-between])

(defelement -close-form-link
  :tag :a
  :class [:block]
  :style [[:svg {:fill (colors :neutral-4)}]
          [:&:hover [:svg {:fill (colors :blue-4)}]]])

(defelement -outer-buttons-wrap
  :style [{:position "sticky"
           :bottom 0}])

(defelement -buttons-gradient
  :style [{:height "1rem"
           :background "linear-gradient(to bottom, rgba(255,255,255,0) 0%, rgba(255,255,255,1) 100%)"}])

(defelement -buttons-wrap
  :class [:flex :justify-between :bwt1 :bd-neutral-7 :py2 :bg-white]
  :style [])

(defn render [ctx]
  (let [route             (route> ctx)
        current-flow-node (sub> ctx :current-flow-node)
        form-type         (sub> ctx :current-flow-node-form-type)
        node-id           (:node-id route)
        form-props        [form-type node-id]
        form-state        (forms-ui/form-state> ctx form-props)
        state             (get-in form-state [:state :type])
        submitting?       (= :submitting state)
        form-renderer     (case form-type
                            :flow-screen   protok.ui.flows.editor.node-form-flow-screen/render
                            :flow-event    protok.ui.flows.editor.node-form-flow-event/render
                            :flow-switch   protok.ui.flows.editor.node-form-flow-switch/render
                            :flow-flow-ref protok.ui.flows.editor.node-form-flow-flow-ref/render
                            nil)]
    (when form-state
      [-form {:on-submit #(<submit-exclusive ctx form-props %)}
       [-form-title-wrap
        [-form-title "Edit " (node-type-name (:type current-flow-node))]
        [-close-form-link
         {:href (ui/url ctx (dissoc route :node-id))}
         (icon :close)]]
       (when form-renderer
         [form-renderer ctx form-props])
       [-outer-buttons-wrap
        [-buttons-gradient]
        [-buttons-wrap
         [buttons/dangerous-secondary-small
          {:on-click    #(<cmd ctx [:flow-editor :delete-node] (:id current-flow-node))
           :button/pill true
           :type        :button}
          "Delete"]
         [:div.flex.items-center
          [buttons/link-small
           {:href (ui/url ctx (dissoc route :node-id))}
           "Cancel"]
          [buttons/primary-small
           {:button/pill true
            :icon/right  (if submitting? :spinner :arrow-forward)
            :disabled    submitting?}
           "Save"]]]]])))

(def component
  (ui/constructor {:renderer render
                   :subscription-deps [:current-flow-nodes
                                       :current-flow-node
                                       :current-flow-node-form-type
                                       :project-file-by-id
                                       :flows]}))
