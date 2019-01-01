(ns protok.ui.organizations.list
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.empty-state :as empty-state]
            [protok.ui.components.buttons :as button]
            [protok.ui.components.data-table :as data-table]
            [keechma.toolbox.ui :refer [sub>]]
            [protok.ui.shared :refer [datasources-pending?>]]))

(defn render-empty-state [ctx]
  [empty-state/render 
   :group 
   [:div.flex.flex-column.items-center
    "You don't belong to an organization yet."
    [:div.mt2
     [button/primary-small
      {:href (ui/url ctx {:page "organizations" :subpage "new"})
       :icon/right :add
       :button/pill true}
      "Create a new organization"]]]])

(defn render-row-actions [ctx d]
  (let [base-url {:page "organizations" :id (:id d)}]
    [:div.right-align
     [button/link-small
      {:href (ui/url ctx (assoc base-url :subpage "edit"))}
      "Edit"]
     [button/primary-small
      {:href (ui/url ctx (assoc base-url :subpage "view"))
       :button/pill true}
      "View"]]))

(defn render-list [ctx]
  (let [organizations (sub> ctx :organizations)]
    [data-table/render
     [{:header/content "Name"
       :key            :name
       :cell/content   :name}
      {:key :actions
       :cell/content [render-row-actions ctx]
       :cell/class [:on-row-hover-reveal]}]
     organizations]))

(defn render [ctx]
  (let [organizations (sub> ctx :organizations)]
    (if-not (seq organizations)
      [render-empty-state ctx]
      [:div
       [render-list ctx]
       [:div.right-align.px2.py3
        [button/secondary-small
         {:href (ui/url ctx {:page "organizations" :subpage "new"})
          :icon/right :add
          :button/pill true}
         "Create a new organization"]]])))

(def component
  (-> (ui/constructor 
       {:renderer render
        :subscription-deps [:organizations]})
      (assoc :protok/config {:layout :content
                             :loading? #(datasources-pending?> % :organizations)})))
