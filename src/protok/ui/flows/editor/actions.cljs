(ns protok.ui.flows.editor.actions
  (:require [keechma.toolbox.entangled.pipeline :as epp]
            [keechma.toolbox.entangled.shared :refer [get-state-app-db-path]]
            [keechma.toolbox.entangled.pipeline :refer [state-changed?]]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.tasks :as t]
            [oops.core :refer [ocall oget oset!]]
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
  (let [flow (edb/get-named-item app-db :flow :current)]
    (edb/prepend-related-collection app-db :flow :flowNodes flow [node])))

(defn center-node [app-db ctx]
  (let [comp-state (get-in app-db (get-state-app-db-path ctx))
        node-id (get-in app-db [:route :data :node-id])
        el (ocall js/document :getElementById (:editor-el comp-state))
        layout (get-in comp-state [:layout :layout])]
    (when (and el layout node-id)
      (let [el-height (oget el :offsetHeight)
            el-width (oget el :offsetWidth)
            node-x (get-in layout [:nodes node-id :x])
            node-y (get-in layout [:nodes node-id :y])
            el-x (/ el-height 2)
            el-y (/ el-width 2)]
        (oset! el :scrollLeft (max 0 (- node-x el-x)))
        (oset! el :scrollTop (max 0 (- node-y el-y)))))))

(def actions
  {:on-init (pipeline! [value app-db ctx]
              (wait-dataloader-pipeline!)
              (epp/comp-commit! (assoc (epp/get-state app-db ctx) :initialized? true)))
   :on-state-change (pipeline! [value app-db ctx]
                      (epp/comp-execute! :calculate-layout))
   :calculate-layout (pipeline! [value app-db ctx]
                       (pp/commit! (layout-calculator/update-layout app-db ctx))
                       (center-node app-db ctx))
   :route-changed (pipeline! [value app-db ctx]
                    (center-node app-db ctx))
   :create-node (pipeline! [value app-db ctx]
                  (create-node! app-db value)
                  (when value
                    (pipeline! [value app-db ctx]
                      (pp/commit! (add-node-to-current-flow app-db (:node value)))
                      (when (:redirect-after? value)
                        (pp/redirect! (assoc (get-in app-db [:route :data]) :node-id (get-in value [:node :id])))))))})
