(ns protok.domain.form-ids
  (:require [clojure.core.match :refer-macros [match]]))

(defn on-page [mount-fn page]
  (fn [route app-db]
    (when (= page (:page route))
      (mount-fn route app-db))))

(def organization
  (-> (fn [route _]
        (match [route]
          [{:subpage "new"}] :new
          [{:subpage "edit" :id id}] id
          :else nil))
      (on-page "organizations")))

(def request-login-code #(when (= "login" (:page %)) :form))
