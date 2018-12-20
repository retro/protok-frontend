(ns protok.domain.db
  (:require [medley.core :refer [dissoc-in]])
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
  :login-requested-for)
