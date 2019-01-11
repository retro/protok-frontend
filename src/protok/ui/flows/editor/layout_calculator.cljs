(ns protok.ui.flows.editor.layout-calculator
  (:require [keechma.toolbox.entangled.shared :refer [get-state-app-db-path]]
            [keechma.toolbox.logging :as l]
            [oops.core :refer [oget ocall oset!]]
            [protok.edb :as edb]
            [com.rpl.specter :as s]
            [dagre]))

(def min-margins
  {:left 50
   :top 50})

(defn calculate-node-left-coordinate [{:keys [x width]}]
  (- x (/ width 2)))

(defn calculate-node-top-coordinate [{:keys [y height]}]
  (- y (/ height 2)))

(defn force-left-margins [layout]
  (let [node-xs (s/select [:nodes s/MAP-VALS (s/parser calculate-node-left-coordinate)] layout)
        edge-xs (s/select [:edges s/MAP-VALS :points s/ALL :x] layout)
        min-x (apply min (concat node-xs edge-xs))
        margin-diff (- (:left min-margins) min-x)
        add-margin #(+ margin-diff %)]
    (if (pos? margin-diff)
      (->> layout
           (s/transform [:dimensions :width] add-margin)
           (s/transform [:nodes s/MAP-VALS :x] add-margin)
           (s/transform [:edges s/MAP-VALS :points s/ALL :x] add-margin))
      layout)))

(defn force-top-margins [layout]
  (let [node-ys (s/select [:nodes s/MAP-VALS (s/parser calculate-node-top-coordinate)] layout)
        edge-ys (s/select [:edges s/MAP-VALS :points s/ALL :y] layout)
        min-y (apply min (concat node-ys edge-ys))
        margin-diff (- (:top min-margins) min-y)
        add-margin #(+ margin-diff %)]
    (if (pos? margin-diff)
      (->> layout
           (s/transform [:dimensions :height] add-margin)
           (s/transform [:nodes s/MAP-VALS :y] add-margin)
           (s/transform [:edges s/MAP-VALS :points s/ALL :y] add-margin))
      layout)))

(defn force-margins [layout]
  (-> layout
      force-left-margins
      force-top-margins))

(defn add-max-edge-index [layout]
  (let [max-index (apply max (filter (complement nil?) (map :index (vals (:edges layout)))))]
    (assoc layout :max-edge-index max-index)))

(def Graph (oget dagre :graphlib.Graph))

(defn all-nodes-have-dimensions? [nodes node-dimensions]
  (let [ids (map :id nodes)
        dimensions (map #(get node-dimensions %) ids)]
    (every? (complement nil?) dimensions)))

(defn extract-switch-edges [nodes node]
  (let [id (:id node)
        options (:options node)
        target-ids (set (s/select [:options s/ALL :targetFlowNode :id] node))
        target-types (s/select [s/ALL (s/pred #(contains? target-ids (:id %))) :type] nodes)
        all-screens? (every? #(= "SCREEN" %) target-types)]
    (map-indexed
     (fn [idx o]
       (when-let [to-id (get-in o [:targetFlowNode :id])]
         {:from id :to to-id :type :switch :index idx :weight (if all-screens? 5 1)}))
     options)))

(defn extract-screen-edges [nodes node]
  (let [id (:id node)
        options (:hotspots node)]
    (map-indexed
     (fn [idx o]
       (when-let [to-id (get-in o [:targetFlowNode :id])]
         {:from id :to to-id :type :hotspot :index idx}))
     options)))

(defn extract-event-edge [nodes node]
  (let [from (:id node)
        to (get-in node [:targetFlowNode :id])]
    (when (and from to)
      {:from from :to to :type :event})))

(defn extract-edges [nodes node]
  (case (:type node)
    "EVENT"  (extract-event-edge nodes node) 
    "SWITCH" (extract-switch-edges nodes node)
    "SCREEN" (extract-screen-edges nodes node)
    nil))

(defn extract-all-edges [{:keys [layout]} nodes]
  (filter (complement nil?) (flatten (map #(extract-edges nodes %) nodes))))

(defn process-nodes [{:keys [layout]} nodes]
  nodes)

(defn calculate [options nodes edges node-dimensions]
  (let [g (Graph.)
        rankdir (if (= :horizontal (:direction options)) "lr" "tb")]
    (ocall g :setGraph #js{:nodesep 50 :ranksep 50 :edgesep 50 :rankdir rankdir :align "dl" :marginx (:left min-margins) :marginy (:top min-margins)})
    (doseq [{:keys [id]} nodes]
      (ocall g :setNode id (clj->js (assoc (get node-dimensions id) :label id))))
    (doseq [{:keys [from to index weight]} edges] 
      (ocall g :setEdge from to #js{:index index :weight (or weight 1)}))
    (ocall dagre :layout g)
    (let [og (ocall g :graph)]
      (-> {:nodes      (into {} (map (fn [n] [n (js->clj (ocall g :node n) :keywordize-keys true)]) (ocall g :nodes)))
           :edges      (into {} (map (fn [e] 
                                        (let [id [(oget e :v) (oget e :w)]
                                              edge (js->clj (ocall g :edge e) :keywordize-keys true)]
                                          [id (assoc edge :node-ids (set id))])) (ocall g :edges)))
           :dimensions {:width  (oget og :width)
                        :height (oget og :height)}}
          force-margins
          add-max-edge-index))))

(defn update-layout [app-db ctx]
  (let [state-path (get-state-app-db-path ctx)
        state (get-in app-db state-path)
        options (:options state)
        node-dimensions (:node-dimensions state)
        flow (edb/get-named-item app-db :flow :current)
        nodes-getter (:flowNodes flow)
        nodes (process-nodes options (nodes-getter))
        edges (extract-all-edges options nodes)
        layout-id (hash [(map :id nodes) edges node-dimensions options])]
    (cond
      (= layout-id (get-in state [:layout :id])) app-db
      (not (all-nodes-have-dimensions? nodes node-dimensions)) app-db
      :else (assoc-in app-db (conj state-path :layout) 
                      {:id layout-id
                       :layout (calculate options nodes edges node-dimensions)}))))
