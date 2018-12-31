(ns protok.ui.flows.list
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.empty-state :as empty-state]
            [protok.ui.components.buttons :as button]
            [protok.ui.components.data-table :as data-table]
            [keechma.toolbox.ui :refer [sub> route>]]))

(defn render-empty-state [ctx]
  (let [id (:id (route> ctx))]
    [empty-state/render 
     :waves
     [:div.flex.flex-column.items-center
      "You haven't created any flows yet."
      [:div.mt2
       [button/primary-small
        {:href (ui/url ctx {:page "flows" :subpage "new" :project-id id})
         :icon/right :add
         :button/pill true}
        "Create a new flow"]]]]))

(defn render-row-actions [ctx d]
  (let [base-url {:page "flows" :id (:id d)}]
    [:div.right-align
     [button/link-small
      {:href (ui/url ctx (assoc base-url :subpage "edit"))}
      "Edit"]
     [button/primary-small
      {:href (ui/url ctx (assoc base-url :subpage "view"))
       :button/pill true}
      "View"]]))

(defn render-list [ctx]
  (let [flows (sub> ctx :flows)]
    [data-table/render
     [{:header/content "Name"
       :key            :name
       :cell/content   :name}
      {:key :actions
       :cell/content [render-row-actions ctx]
       :cell/class [:on-row-hover-reveal]}]
     flows]))

(defn render [ctx]
  (let [flows (sub> ctx :flows)
        id (:id (route> ctx))]
    (if-not (seq flows)
      [render-empty-state ctx]
      [:div
       [render-list ctx]
       [:div.right-align.px2.py3
        [button/secondary-small
         {:href (ui/url ctx {:page "flows" :subpage "new" :project-id id})
          :icon/right :add
          :button/pill true}
         "Create a new flow"]]])))

(def component
  (-> (ui/constructor {:renderer render
                       :subscription-deps [:flows]})
      (assoc :protok/config {:layout :content})))
