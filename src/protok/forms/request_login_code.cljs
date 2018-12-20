
(ns protok.forms.request-login-code
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db form-props]
  {:email (get-in app-db [:kv :login-requested-for])})

(defmethod forms-core/submit-data Form [_ app-db _ data]
  data)

(defmethod forms-core/on-submit-success Form [this app-db form-props data]
  (pipeline! [value app-db]
    (pp/commit! (assoc-in app-db [:kv :login-requested-for] (:email data)))
    (pp/send-command! [forms-core/id-key :mount-form] [:login-with-code :form])))

(defn constructor []
  (->Form (v/to-validator {:email [:not-empty :email]})))
