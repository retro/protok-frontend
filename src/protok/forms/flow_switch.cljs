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
            [promesa.core :as p]
            [clojure.set :as set]))

(defn save-option! [app-db flow-node-id option]
  (let [new? (not (:id option))
        option' (-> option
                    (assoc :targetFlowNodeId (get-in option [:targetFlowNode :id]))
                    (dissoc :targetFlowNode)
                    (as-> d (if new? (assoc d :flowNodeId flow-node-id) d)))
        mutation (if new? :create-flow-switch-option :update-flow-switch-option)]
    (gql/m! mutation {:input option'} (db/get-jwt app-db))))

(defn save-options! [app-db flow-node-id options]
  (p/all (map #(save-option! app-db flow-node-id %) options)))

(defn delete-option! [app-db option-id]
  (gql/m! :delete-flow-switch-option {:id option-id} (db/get-jwt app-db)))

(defn delete-options! [app-db form-props data]
  (let [initial-options (get-in app-db [:kv forms-core/id-key :states form-props :initial-data :options])
        initial-options-ids (set (filter (complement nil?) (map :id initial-options)))
        options-ids (set (filter (complement nil?) (map :id (:options data))))
        missing-ids (seq (set/difference initial-options-ids options-ids))]
   (when missing-ids
     (p/all (map #(delete-option! app-db %) missing-ids)))))

(defn prepare-data [data]
  (-> data
      (dissoc :type :options)))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (db/get-current-flow-node app-db)))

(defmethod forms-core/submit-data Form [_ app-db form-props data]
  (let [[_ id] form-props]
    (pipeline! [value app-db]
      (delete-options! app-db form-props data)
      (save-options! app-db id (:options data))
      (gql/m!
       [:update-flow-switch :updateFlowSwitch]
       {:input (prepare-data data)}
       (db/get-jwt app-db)))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (pp/commit! (edb/insert-item app-db :flow-node data))
    (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id))))

(defn constructor []
  (->Form (v/to-validator {})))
