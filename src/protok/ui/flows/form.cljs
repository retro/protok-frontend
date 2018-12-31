(ns protok.ui.flows.form
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [route>]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]
            [protok.domain.form-ids :as form-ids]
            [protok.ui.shared :refer [<submit-exclusive]]))

(defn render [ctx]
  (let [form-props [:flow (form-ids/flow (route> ctx))]
        form-state (forms-ui/form-state> ctx form-props)
        state (get-in form-state [:state :type])
        submitting? (= :submitting state)]
    (when form-state
      [:form.p2 {:on-submit #(<submit-exclusive ctx form-props %)}
       [inputs/text ctx form-props :name
        {:label "Flow name"
         :placeholder "Flow"
         :auto-focus true}]
        [:div.flex.justify-end
         [buttons/primary-small
          {:button/pill true
           :icon/right (when submitting? :spinner)
           :disabled submitting?}
          "Save Flow"]]])))

(def component
  (-> (ui/constructor {:renderer render})
      (assoc :protok/config {:layout :content})))
