(ns protok.forms.project
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]
            [protok.gql :as gql]
            [protok.util.local-storage :refer [ls-set!]]
            [protok.settings :refer [jwt-ls-name]]
            [protok.edb :as edb]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]))

(defn prepare-data [data]
  (select-keys data [:id :name :organization-id]))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (if (= :new id)
      {:organization-id (get-in app-db [:route :data :organization-id])}
      (edb/get-item-by-id app-db :project id))))

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (gql/m!
     (if (= :new id)
       [:create-project :createProject]
       [:update-project :updateProject])
     {:input (prepare-data data)}
     (db/get-jwt app-db))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (if (= :new id)
      (pp/commit! (edb/prepend-collection app-db :project :list [data]))
      (pp/commit! (edb/insert-item app-db :project data)))
    (pp/redirect! {:page "projects" :subpage "view" :id (:id data)})))

(defn constructor []
  (->Form (v/to-validator {:name [:not-empty]})))
