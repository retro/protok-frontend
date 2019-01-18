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
            [promesa.core :as p]
            [clojure.set :as set]
            [clojure.string :as str]))

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

(defn delete-hotspot! [app-db hotspot-id]
  (gql/m! :delete-flow-screen-hotspot {:id hotspot-id} (db/get-jwt app-db)))

(defn delete-hotspots! [app-db form-props data]
  (let [initial-hotspots (get-in app-db [:kv forms-core/id-key :states form-props :initial-data :hotspots])
        initial-hotspots-ids (set (filter (complement nil?) (map :id initial-hotspots)))
        hotspots-ids (set (filter (complement nil?) (map :id (:hotspots data))))
        missing-ids (seq (set/difference initial-hotspots-ids hotspots-ids))]
   (when missing-ids
     (p/all (map #(delete-hotspot! app-db %) missing-ids)))))

(defn prepare-data [data]
  (-> data
      (assoc :projectFileId (get-in data [:projectFile :id]))
      (dissoc :type :hotspots :projectFile)))

(defrecord Form [validator])

(defmethod forms-core/process-attr-with Form [this path]
  (when (= [:projectFile] path)
    (fn [app-db form-props form-state path value]
      (let [filename (-> (str/split (:serverFilename value) "/")
                         last
                         (str/split ".")
                         first)
            project-name (str/trim (or (get-in form-state [:data :name]) ""))
            project-name' (if (empty? project-name) filename project-name)]
        (-> form-state
            (assoc-in [:data :name] project-name')
            (assoc-in [:data :projectFile] value))))))

(defmethod forms-core/get-data Form [this app-db [_ id]]
  (pipeline! [value app-db]
    (wait-dataloader-pipeline!)
    (db/get-current-flow-node app-db)))

(defmethod forms-core/submit-data Form [_ app-db form-props data]
  (let [[_ id] form-props]
    (pipeline! [value app-db]
      (delete-hotspots! app-db form-props data)
      (save-hotspots! app-db id (:hotspots data))
      (gql/m!
       [:update-flow-screen :updateFlowScreen]
       {:input (prepare-data data)}
       (db/get-jwt app-db)))))

(defmethod forms-core/on-submit-success Form [this app-db [_ id] data]
  (pipeline! [value app-db]
    (pp/commit! (edb/insert-item app-db :flow-node data))
    (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id))))

(defn constructor []
  (->Form (v/to-validator {})))
