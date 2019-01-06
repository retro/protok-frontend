(ns protok.ui.flows.editor.node-form-flow-event
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
            [protok.ui.flows.editor.node-form-shared :refer [flow-node-select]]))

(defn render [ctx form-props]
  [:<>
   [inputs/text ctx form-props :name 
    {:label "Name"
     :placeholder "Name"}]
   [inputs/textarea ctx form-props :description
    {:label "Description"}]
   [flow-node-select ctx form-props :targetFlowNode.id]])
