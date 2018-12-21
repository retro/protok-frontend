(ns protok.subscriptions
  (:require [protok.edb :refer [edb-schema]]
            [protok.domain.db :as db])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn as-sub [getter-fn]
  (fn [app-db-atom & args]
    (reaction
     (apply getter-fn @app-db-atom args))))

(def subscriptions
  {:initialized? (as-sub db/get-initialized?)
   :account-menu-open? (as-sub db/get-account-menu-open?)})
