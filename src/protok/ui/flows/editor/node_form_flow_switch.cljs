(ns protok.ui.flows.editor.node-form-flow-switch
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
            [protok.ui.flows.editor.node-form-shared :refer [render-hotspots-options -form-subtitle]]))

(defn render [ctx form-props]
  (let [form-state (forms-ui/form-state> ctx form-props)
        options (or (forms-ui/value-in> ctx form-props :options) [])]
    [:<>
     [inputs/text ctx form-props :name 
      {:label "Name"
       :placeholder "Name"}]
     [inputs/textarea ctx form-props :description
      {:label "Description"}]
     [:div.bwt1.bd-neutral-7.mt2.pt2
      [-form-subtitle "Options"]
      (map-indexed
       (fn [idx o]
         ^{:key idx}
         [render-hotspots-options ctx form-props options :options idx])
       options)
      [:div.flex.justify-end
       [buttons/secondary-small
        {:on-click #(forms-ui/<set-value ctx form-props :options (conj options {}))
         :type :button
         :button/pill true}
        "Add Option"]]]]))
