(ns protok.domain.db
  (:require [medley.core :refer [dissoc-in]]
            [protok.edb :as edb]
            [camel-snake-kebab.core :refer [->kebab-case-keyword]])
  (:require-macros [protok.domain.db :refer [defkvaccess]]))

(defn path->kv-path [path]
  (if (vector? path)
    (concat [:kv] path)
    [:kv path]))

(defn make-get-in-kv [path]
  (fn [app-db]
    (get-in app-db (path->kv-path path))))

(defn make-assoc-in-kv [path]
  (fn [app-db value]
    (assoc-in app-db (path->kv-path path) value)))

(defn make-dissoc-in-kv [path]
  (fn [app-db]
    (dissoc-in app-db (path->kv-path path))))

(defkvaccess
  :jwt
  :login-requested-for
  :initialized?
  :account-menu-open?)

(defn get-current-account [app-db]
  (edb/get-named-item app-db :account :current))

(defn set-current-account [app-db current-account]
  (edb/insert-named-item app-db :account :current current-account))

(defn remove-current-account [app-db]
  (edb/remove-named-item app-db :account :current))

(defn get-current-flow-node [app-db]
  (when-let [id (get-in app-db [:route :data :node-id])]
    (edb/get-item-by-id app-db :flow-node id)))

(defn get-current-flow-node-form-type [app-db]
  (when-let [node (get-current-flow-node app-db)]
    (->kebab-case-keyword (str "flow_" (:type node)))))

(defn get-current-flow-nodes [app-db]
  (:flowNodes (edb/get-named-item app-db :flow :current false [:flowNodes])))
