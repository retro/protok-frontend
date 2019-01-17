(ns protok.forms.invite-organization-member
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]
            [protok.domain.gql :refer [organization-processor]]
            [protok.gql :as gql]
            [protok.util.local-storage :refer [ls-set!]]
            [protok.settings :refer [jwt-ls-name]]
            [protok.edb :as edb]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline! run-dataloader!]]))

(defrecord Form [validator])

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (gql/m! [:invite-organization-member :inviteOrganizationMember]
            {:email (:email data)
             :organizationId (get-in app-db [:route :data :id])}
            (db/get-jwt app-db))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (run-dataloader! [:current-organization])
    (pp/send-command! [forms-core/id-key :mount-form] [:invite-organization-member (get-in app-db [:route :data :id])])))

(defn constructor []
  (->Form (v/to-validator {:email [:not-empty :email]})))
