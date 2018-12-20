(ns protok.util.local-storage
  (:require [hodgepodge.core :refer [local-storage get-item set-item remove-item]]))

(defn ls-get [key]
  (get-item local-storage key))

(defn ls-set! [key value]
  (set-item local-storage key value))

(defn ls-remove! [key]
  (remove-item local-storage key))

