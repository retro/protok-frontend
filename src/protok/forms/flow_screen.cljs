(ns protok.forms.flow-screen
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

(defn save-hotspot! [app-db flow-node-id hotspot]
  (let [new? (not (:id hotspot))
        hotspot' (-> hotspot
                    (assoc :targetFlowNodeId (get-in hotspot [:targetFlowNode :id]))
                    (dissoc :targetFlowNode)
                    (as-> h (if new? (assoc h :flowNodeId flow-node-id) h)))
        mutation (if new? :create-flow-screen-hotspot :update-flow-screen-hotspot)]
    (gql/m! mutation {:input hotspot'} (db/get-jwt app-db))))

(defn save-hotspots! [app-db flow-node-id hotspots]
  (p/all (map #(save-hotspot! app-db flow-node-id %) hotspots)))

(defn prepare-data [data]
  (-> data
      (assoc :projectFileId (get-in data [:projectFile :id]))
      (dissoc :type :hotspots :projectFile)))

(defrecord Form [validator])

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (db/get-current-flow-node app-db)))

(defmethod forms-core/submit-data Form [_ app-db [_ id] data]
  (pipeline! [value app-db]
    (save-hotspots! app-db id (:hotspots data))
    (gql/m!
     [:update-flow-screen :updateFlowScreen]
     {:input (prepare-data data)}
     (db/get-jwt app-db))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (pp/commit! (edb/insert-item app-db :flow-node data))
    (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id))))

(defn constructor []
  (->Form (v/to-validator {})))
