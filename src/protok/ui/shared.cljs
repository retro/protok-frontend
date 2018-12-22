(ns protok.ui.shared
  (:require [keechma.toolbox.ui :refer [sub>]]
            [keechma.toolbox.forms.ui :as forms-ui]
            [keechma.toolbox.dataloader.core :as dataloader]))

(defn datasources-pending?> [ctx & datasources]
  (let [app-db (deref (:app-db ctx))
        dl-state (get-in app-db [:kv dataloader/id-key])
        ds-statuses (into {} (map (fn [[k v]] [k (:status v)]) dl-state))
        filtered-ds-statuses (if (seq datasources)
                               (select-keys ds-statuses (flatten datasources))
                               ds-statuses)]
    (some #(= :pending %) (vals filtered-ds-statuses))))

(defn <submit-exclusive [ctx form-props ev]
  (let [state (get-in (forms-ui/form-state> ctx form-props) [:state :type])]
    (if (not= :submitting state)
      (forms-ui/<submit ctx form-props ev)
      (.preventDefault ev))))
