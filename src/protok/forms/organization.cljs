(ns protok.forms.organization
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]
            [protok.domain.gql :refer [mutate-organization-processor]]
            [protok.gql :as gql]
            [protok.util.local-storage :refer [ls-set!]]
            [protok.settings :refer [jwt-ls-name]]
            [protok.edb :as edb]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]))

(defn prepare-data [data]
  (select-keys data [:id :name]))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (when-not (= :new id)
      (edb/get-item-by-id app-db :organization id))))

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (gql/m!
     (if (= :new id)
       [:create-organization :createOrganization]
       [:update-organization :updateOrganization])
     {:organization (prepare-data data)}
     (db/get-jwt app-db))
    (mutate-organization-processor value)))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (if (= :new id)
      (pp/commit! (edb/prepend-collection app-db :organization :list [data]))
      (pp/commit! (edb/insert-item app-db :organization data)))
    (pp/redirect! {:page "organizations" :subpage "view" :id (:id data)})))

(defn constructor []
  (->Form (v/to-validator {:name [:not-empty]})))
