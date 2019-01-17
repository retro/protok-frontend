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

(def project
  (-> (fn [route _]
        (match [route]
          [{:subpage "new"}] :new
          [{:subpage "edit" :id id}] id
          :else nil))
      (on-page "projects")))

(def flow
  (-> (fn [route _]
        (match [route]
          [{:subpage "new"}] :new
          [{:subpage "edit" :id id}] id
          :else nil))
      (on-page "flows")))

(def request-login-code #(when (= "login" (:page %)) :form))

(def invite-organization-member
   (-> (fn [route _]
         (match [route]
           [{:subpage "edit" :id id}] id
           :else nil))
      (on-page "organizations")))
