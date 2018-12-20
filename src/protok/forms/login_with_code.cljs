(ns protok.forms.login-with-code
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]))

(defrecord Form [validator])

(defmethod forms-core/call Form [form app-db form-props [cmd & args]]
  (when (= :reset-login-flow cmd)
    (pipeline! [value app-db]
      (pp/send-command! [forms-core/id-key :mount-form] [:request-login-code :form])
      (pp/send-command! [forms-core/id-key :unmount-form] [:login-with-code :form]))))

(defmethod forms-core/get-data Form [this app-db form-props]
  {:email (db/get-login-requested-for app-db)})

(defmethod forms-core/submit-data Form [_ app-db _ data])

(defmethod forms-core/on-submit-success Form [this app-db form-props data]
  (pipeline! [value app-db]))

(defn constructor []
  (->Form (v/to-validator {:email [:not-empty :email]
                           :code [:not-empty]})))
