(ns protok.forms.login-with-code
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]
            [protok.gql :as gql]
            [protok.util.local-storage :refer [ls-set!]]
            [protok.settings :refer [jwt-ls-name]]))

(defrecord Form [validator])

(defmethod forms-core/call Form [form app-db form-props [cmd & args]]
  (when (= :reset-login-flow cmd)
    (pipeline! [value app-db]
      (pp/send-command! [forms-core/id-key :mount-form] [:request-login-code :form])
      (pp/send-command! [forms-core/id-key :unmount-form] [:login-with-code :form]))))

(defmethod forms-core/get-data Form [this app-db form-props]
  {:email (db/get-login-requested-for app-db)})

(defmethod forms-core/submit-data Form [_ app-db _ data]
  (gql/m! [:login-with-code :loginWithCode] data))

(defmethod forms-core/on-submit-success Form [this app-db form-props {:keys [account token] :as data}]
  (pipeline! [value app-db]
    (pp/commit!
     (-> app-db
         (db/assoc-jwt token)
         (db/set-current-account account)))
    (ls-set! jwt-ls-name token)
    (pp/reroute!)))

(defn constructor []
  (->Form (v/to-validator {:email [:not-empty :email]
                           :code [:not-empty]})))
