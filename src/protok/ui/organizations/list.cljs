(ns protok.ui.organizations.list
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.empty-state :as empty-state]
            [protok.ui.components.buttons :as button]
            [keechma.toolbox.ui :refer [sub>]]))

(defn render [ctx]
  (let [organizations (sub> ctx :organizations)]
    [:div
     (if-not (seq organizations)
       [empty-state/render 
        :group 
        [:div.flex.flex-column.items-center
         "You don't belong to an organization yet."
         [:div.mt2
          [button/primary-small
           {:href (ui/url ctx {:page "organizations" :subpage "new"})
            :icon/right :add
            :button/pill true}
           "Create a new organization"]]]])]))

(def component
  (ui/constructor {:renderer render
                   :subscription-deps [:organizations]}))
