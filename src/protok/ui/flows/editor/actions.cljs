(ns protok.ui.flows.editor.actions
  (:require [keechma.toolbox.entangled.pipeline :as epp]
            [keechma.toolbox.entangled.shared :refer [get-state-app-db-path get-id]]
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
            [protok.ui.flows.editor.layout-calculator :as layout-calculator]
            [protok.util :refer [delay-pipeline]])
  (:import [goog.async Throttle]))

(defn current-flow-id [app-db]
  (:id (edb/get-named-item app-db :flow :current)))

(defn create-screen! [app-db]
  (gql/m! [:create-flow-screen :createFlowScreen]
          {:input {:name ""
                   :flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-event! [app-db]
  (gql/m! [:create-flow-event :createFlowEvent]
          {:input {:name ""
                   :flow-id (current-flow-id app-db)}}
          (db/get-jwt app-db)))

(defn create-switch! [app-db]
  (gql/m! [:create-flow-switch :createFlowSwitch]
          {:input {:name ""
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

(def scroll-movement-per-frame 30)

(defn scrollable? [val]
  (< scroll-movement-per-frame (ocall js/Math :abs val)))

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
            el-y (/ el-width 2)
            scroll-left (oget el :scrollLeft)
            scroll-top (oget el :scrollTop)
            target-scroll-left (max 0 (- node-x el-x))
            target-scroll-top (max 0 (- node-y el-y))
            scroll-left-diff (- target-scroll-left scroll-left)
            scroll-top-diff (- target-scroll-top scroll-top)]
        (if (or (scrollable? scroll-left-diff)
                (scrollable? scroll-top-diff)) 
          (let [max-scroll-diff (max (ocall js/Math :abs scroll-left-diff) (ocall js/Math :abs scroll-top-diff))
                frame-count (ocall js/Math :ceil (/ max-scroll-diff scroll-movement-per-frame))]
            (t/blocking-raf!
             [::scroll (get-id ctx)]
             (fn [{:keys [times-invoked id]} app-db]
               (let [scroll-left' (oget el :scrollLeft)
                     scroll-top' (oget el :scrollTop)
                     frame-target-scroll-left (+ scroll-left' (/ scroll-left-diff frame-count))
                     frame-target-scroll-top (+ scroll-top' (/ scroll-top-diff frame-count))
                     last-frame? (= times-invoked frame-count)]
                 (oset! el :scrollLeft (if last-frame? target-scroll-left frame-target-scroll-left))
                 (oset! el :scrollTop (if last-frame? target-scroll-top frame-target-scroll-top))
                 (if (= times-invoked frame-count)
                   (t/stop-task app-db id)
                   app-db)))))
          (do
            (oset! el :scrollLeft target-scroll-left)
            (oset! el :scrollTop target-scroll-top)))))))

(def actions
  {:on-init (pipeline! [value app-db ctx]
              (wait-dataloader-pipeline!)
              (epp/comp-commit! (assoc (epp/get-state app-db ctx)
                                       :initialized? true
                                       :options {:direction :vertical
                                                 :zoom :actual})))
   :on-state-change (pipeline! [value app-db ctx]
                      (epp/comp-execute! :calculate-layout))
   :calculate-layout (pipeline! [value app-db ctx]
                       (layout-calculator/get-layout app-db ctx)
                       (when value
                         (pipeline! [value app-db ctx]
                           (epp/comp-swap! assoc :layout value)
                           (epp/comp-execute! :center-node))))
   :route-changed (pipeline! [value app-db ctx]
                    (epp/comp-execute! :center-node))
   :center-node (pipeline! [value app-db ctx]
                  (delay-pipeline 50)
                  (center-node app-db ctx))
   :create-node (pipeline! [value app-db ctx]
                  (create-node! app-db value)
                  (when value
                    (pipeline! [value app-db ctx]
                      (pp/commit! (add-node-to-current-flow app-db (:node value)))
                      (when (:redirect-after? value)
                        (pp/redirect! (assoc (get-in app-db [:route :data]) :node-id (get-in value [:node :id])))))))
   :highlight-edge (pp/exclusive
                    (pipeline! [value app-db ctx]
                      (delay-pipeline 100)
                      (epp/comp-swap! assoc :active-edge-id value)))})
