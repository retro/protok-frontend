(ns protok.forms.flow-event
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
  (let [target-flow-node-id (get-in data [:targetFlowNode :id])]
    (-> data
        (assoc :targetFlowNodeId target-flow-node-id)
        (dissoc :type :targetFlowNode))))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (db/get-current-flow-node app-db)))

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (gql/m!
     [:update-flow-event :updateFlowEvent]
     {:input (prepare-data data)}
     (db/get-jwt app-db))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (pp/commit! (edb/insert-item app-db :flow-node data))
    (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id))))

(defn constructor []
  (->Form (v/to-validator {:name [:not-empty]})))
