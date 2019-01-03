(ns protok.ui.flows.editor.layout-calculator
  (:require [keechma.toolbox.entangled.shared :refer [get-state-app-db-path]]
            [oops.core :refer [oget ocall]]
            [protok.edb :as edb]
            [dagre]))

(def Graph (oget dagre :graphlib.Graph))

(defn all-nodes-have-dimensions? [nodes node-dimensions]
  (let [ids (map :id nodes)
        dimensions (map #(get node-dimensions %) ids)]
    (every? (complement nil?) dimensions)))

(defn extract-switch-edges [node]
  (let [id (:id node)
        options (:options node)]
    (map
     (fn [o]
       (when-let [to-id (get-in o [:targetFlowNode :id])]
         {:from id :to to-id}))
     options)))

(defn extract-edges [node]
  (case (:type node)
    "EVENT" {:from (:id node) :to (get-in node [:targetFlowNode :id])}
    "SWITCH" (extract-switch-edges node)
    nil))

(defn extract-all-edges [nodes]
  (filter (complement nil?) (flatten (map extract-edges nodes))))

(defn calculate [nodes edges node-dimensions]
  (let [g (Graph.)]
    (ocall g :setGraph #js{})
    (doseq [{:keys [id]} nodes]
      (ocall g :setNode id (clj->js (assoc (get-in node-dimensions id) :label id))))
    (doseq [{:keys [from to]} edges]
      (ocall g :setEdge from to #js{}))
    (ocall dagre :layout g #js{:rankdir "LR" :align "DR"})
    (let [og (ocall g :graph)]
      {:nodes      (into {} (map (fn [n] [n (js->clj (ocall g :node n) :keywordize-keys true)]) (ocall g :nodes)))
       :edges      (mapv (fn [e] (:points (js->clj (ocall g :edge e) :keywordize-keys true))) (ocall g :edges))
       :dimensions {:width  (oget og :width)
                    :height (oget og :height)}})))

(defn update-layout [app-db ctx]
  (let [state-path (get-state-app-db-path ctx)
        state (get-in app-db state-path)
        node-dimensions (:node-dimensions state)
        flow (edb/get-named-item app-db :flow :current)
        nodes-getter (:flowNodes flow)
        nodes (nodes-getter)
        edges (extract-all-edges nodes)
        layout-id (hash [(map :id nodes) edges node-dimensions])]

    (cond
      (= layout-id (get-in state [:layout :id])) app-db
      (not (all-nodes-have-dimensions? nodes node-dimensions)) app-db
      :else (assoc-in app-db (conj state-path :layout) 
                      {:id layout-id
                       :layout (calculate nodes edges node-dimensions)}))))
