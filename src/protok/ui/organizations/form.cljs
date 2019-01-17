(ns protok.ui.organizations.form
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [route> sub>]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]
            [protok.domain.form-ids :as form-ids]
            [protok.ui.shared :refer [<submit-exclusive]]))

(defn render-invite-organization-member-form [ctx]
  (let [form-props [:invite-organization-member (form-ids/organization (route> ctx))]
        form-state (forms-ui/form-state> ctx form-props)
        state (get-in form-state [:state :type])
        submitting? (= :submitting state)]
    (when form-state
     [:div.p2
      [:div.c-neutral-2.fs3.mb2 "Invite Member"] 
      [:form {:on-submit #(<submit-exclusive ctx form-props %)}
       [inputs/text ctx form-props :email
        {:label "Email"
         :placeholder "email@example.com"}]
       [:div.flex.justify-end
        [buttons/primary-small
         {:button/pill true
          :icon/right (when submitting? :spinner)
          :disabled submitting?}
         "Invite Member"]]]])))

(defn render [ctx]
  (let [form-props [:organization (form-ids/organization (route> ctx))]
        form-state (forms-ui/form-state> ctx form-props)
        state (get-in form-state [:state :type])
        submitting? (= :submitting state)]
    (when form-state
      [:div
       [:form.p2 {:on-submit #(<submit-exclusive ctx form-props %)}
        [inputs/text ctx form-props :name
         {:label "Organization name"
          :placeholder "Organization"
          :auto-focus true}]
        [:div.flex.justify-end
         [buttons/primary-small
          {:button/pill true
           :icon/right (when submitting? :spinner)
           :disabled submitting?}
          "Save Organization"]]]
       (when-let [organization (sub> ctx :current-organization)]
         [:div.px2.pb2
          [:hr.bd-neutral-8.bw1.mb2]
          [:div.c-neutral-2.fs3.mb2 "Members"]
          [:table.bw1.bd-neutral-8.w100p.c-neutral-3
           [:thead
            [:tr
             [:th.p1.bd-neutral-8.bw1 "Email"]
             [:th.p1.bd-neutral-8.bw1 "Role"]]]
           [:tbody
            (map
             (fn [m]
               ^{:key (get-in m [:account :id])}
               [:tr
                [:td.p1.bd-neutral-8.bw1
                 (get-in m [:account :email])]
                [:td.p1.bd-neutral-8.bw1
                 (:memberRole m)]])
             (:memberships organization))]]])
       [render-invite-organization-member-form ctx]])))

(def component
  (-> (ui/constructor {:renderer render
                       :subscription-deps [:current-organization]})
      (assoc :protok/config {:layout :content})))
