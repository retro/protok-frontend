(ns protok.ui.projects.list
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.empty-state :as empty-state]
            [protok.ui.components.buttons :as button]
            [protok.ui.components.data-table :as data-table]
            [keechma.toolbox.ui :refer [sub> route>]]
            [protok.ui.shared :refer [datasources-pending?>]]))

(defn render-empty-state [ctx]
  (let [id (:id (route> ctx))]
    [empty-state/render 
     :view-list
     [:div.flex.flex-column.items-center
      "You haven't created any projects yet"
      [:div.mt2
       [button/primary-small
        {:href (ui/url ctx {:page "projects" :subpage "new" :organization-id id})
         :icon/right :add
         :button/pill true}
        "Create a new project"]]]]))

(defn render-row-actions [ctx d]
  (let [base-url {:page "projects" :id (:id d)}]
    [:div.right-align
     [button/link-small
      {:href (ui/url ctx (assoc base-url :subpage "edit"))}
      "Edit"]
     [button/primary-small
      {:href (ui/url ctx (assoc base-url :subpage "view"))
       :button/pill true}
      "View"]]))

(defn render-list [ctx]
  (let [projects (sub> ctx :projects)]
    [data-table/render
     [{:header/content "Name"
       :key            :name
       :cell/content   :name}
      {:key :actions
       :cell/content [render-row-actions ctx]
       :cell/class [:on-row-hover-reveal]}]
     projects]))

(defn render [ctx]
  (let [projects (sub> ctx :projects)
        id (:id (route> ctx))]
    (if-not (seq projects)
      [render-empty-state ctx]
      [:div
       [render-list ctx]
       [:div.right-align.px2.py3
        [button/secondary-small
         {:href (ui/url ctx {:page "projects" :subpage "new" :organization-id id})
          :icon/right :add
          :button/pill true}
         "Create a new project"]]])))

(def component
  (-> (ui/constructor {:renderer render
                       :subscription-deps [:projects]})
      (assoc :protok/config {:layout :content
                             :loading? #(datasources-pending?> % :projects)})))
