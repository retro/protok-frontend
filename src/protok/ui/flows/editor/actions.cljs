(ns protok.ui.flows.editor.actions
  (:require [keechma.toolbox.entangled.pipeline :as epp]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.tasks :as t]
            [oops.core :refer [ocall oget]]
            [cljs.core.async :refer [put!]]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]
            [protok.domain.db :as db]
            [protok.gql :as gql]
            [promesa.core :as p]
            [protok.edb :as edb]
            [protok.ui.flows.editor.layout-calculator :as layout-calculator])
  (:import [goog.async Throttle]))

(defn current-flow-id [app-db]
  (:id (edb/get-named-item app-db :flow :current)))

(defn create-screen! [app-db]
  (gql/m! [:create-flow-screen :createFlowScreen]
          {:input {:name "Untitled"
                   :flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-event! [app-db]
  (gql/m! [:create-flow-event :createFlowEvent]
          {:input {:name "Untitled"
                   :flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-switch! [app-db]
  (gql/m! [:create-flow-switch :createFlowSwitch]
          {:input {:name "Untitled"
                   :flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-flow-ref! [app-db]
  (gql/m! [:create-flow-flow-ref :createFlowFlowRef]
          {:input {:flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-node! [app-db [node-type redirect-after?]]
  (->> (case node-type
         :flow-screen   (create-screen! app-db)
         :flow-event    (create-event! app-db)
         :flow-switch   (create-switch! app-db)
         :flow-flow-ref (create-flow-ref! app-db)
         nil)
       (p/map (fn [res]
                (when res
                  {:redirect-after? redirect-after?
                   :node res})))))

(defn add-node-to-current-flow [app-db node]
  (let [flow (edb/get-named-item app-db :flow :current)
        nodes (conj (or (:flowNodes flow) []) node)]
    (js/throw "FIX HERE")
    ;;(edb/insert-named-item app-db :flow :current (assoc flow :flowNodes nodes))
    ))

(def actions
  {:on-init (pipeline! [value app-db ctx]
              (wait-dataloader-pipeline!)
              (epp/comp-commit! (assoc (epp/get-state app-db ctx) :initialized? true)))
   :on-state-change (pipeline! [value app-db ctx]
                      (epp/comp-execute! :calculate-layout))
   :calculate-layout (pipeline! [value app-db ctx]
                       (pp/commit! (layout-calculator/update-layout app-db ctx)))
   :create-node (pipeline! [value app-db ctx]
                  (create-node! app-db value)
                  (when value
                    (pipeline! [value app-db ctx]
                      (pp/commit! (add-node-to-current-flow app-db (:node value)))
                      (when (:redirect-after? value)
                        (pp/redirect! (assoc (get-in app-db [:route :data]) :node-id (get-in value [:node :id])))))))})
