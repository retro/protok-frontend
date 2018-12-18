(ns protok.subscriptions
  (:require [keechma.toolbox.dataloader.subscriptions :as dataloader]
            [protok.edb :refer [edb-schema]]
            [protok.datasources  :refer [datasources]])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn get-kv [key]
  (fn [app-db-atom]
    (reaction
     (get-in @app-db-atom (flatten [:kv key])))))

(def subscriptions
  {})
