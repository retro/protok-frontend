(ns protok.ui.flows.editor.node-form-flow-flow-ref
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



(defn render [ctx form-props]
  (let [current-flow-id (:id (route> ctx))
        flows (sort-by :name (filter #(not= current-flow-id (:id %)) (sub> ctx :flows)))]
    [inputs/select ctx form-props [:targetFlow :id]
     {:label "Target Flow"
      :placeholder "Select Target Flow"
      :options (map (fn [f] {:value (:id f) :label (:name f)}) flows)}]))
