(ns protok.subscriptions
  (:require [protok.edb :refer [edb-schema]]
            [protok.domain.db :as db])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn as-sub [getter-fn]
  (fn [app-db-atom & args]
    (reaction
     (apply getter-fn @app-db-atom args))))

(def subscriptions
  {:initialized?                (as-sub db/get-initialized?)
   :account-menu-open?          (as-sub db/get-account-menu-open?)
   :current-flow-node           (as-sub db/get-current-flow-node)
   :current-flow-node-form-type (as-sub db/get-current-flow-node-form-type)
   :current-flow-nodes          (as-sub db/get-current-flow-nodes)
   :project-file-by-id          (as-sub db/get-project-file-by-id)})
