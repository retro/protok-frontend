(ns protok.forms.flow-switch
  (:require [keechma.toolbox.forms.core :as forms-core]
            [protok.forms.validators :as v]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [protok.domain.db :as db]
            [protok.gql :as gql]
            [protok.util.local-storage :refer [ls-set!]]
            [protok.settings :refer [jwt-ls-name]]
            [protok.edb :as edb]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]
            [promesa.core :as p]))

(defn save-option! [app-db flow-node-id option]
  (let [new? (not (:id option))
        option' (-> option
                    (assoc :targetFlowNodeId (get-in option [:targetFlowNode :id])
                           :flowNodeId flow-node-id)
                    (dissoc :targetFlowNode))
        mutation (if new? :create-flow-switch-option :update-flow-switch-option)]
    (gql/m! mutation {:input option'} (db/get-jwt app-db))))

(defn save-options! [app-db flow-node-id options]
  (p/all (map #(save-option! app-db flow-node-id %) options)))

(defn prepare-data [data]
  (-> data
      (dissoc :type :options)))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (db/get-current-flow-node app-db)))

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (save-options! app-db id (:options data))
    (gql/m!
     [:update-flow-switch :updateFlowSwitch]
     {:input (prepare-data data)}
     (db/get-jwt app-db))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (pp/commit! (edb/insert-item app-db :flow-node data))
    (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id))))

(defn constructor []
  (->Form (v/to-validator {})))
