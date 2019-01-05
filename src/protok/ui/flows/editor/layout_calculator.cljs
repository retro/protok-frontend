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
         {:from id :to to-id :type :switch}))
     options)))

(defn extract-screen-edges [node]
  (let [id (:id node)
        options (:hotspots node)]
    (map
     (fn [o]
       (when-let [to-id (get-in o [:targetFlowNode :id])]
         {:from id :to to-id :type :hotspot}))
     options)))

(defn extract-edges [node]
  (case (:type node)
    "EVENT" {:from (:id node) :to (get-in node [:targetFlowNode :id]) :type :event}
    "SWITCH" (extract-switch-edges node)
    "SCREEN" (extract-screen-edges node)
    nil))

(defn extract-all-edges [nodes]
  (filter (complement nil?) (flatten (map extract-edges nodes))))

(defn calculate [nodes edges node-dimensions]
  (let [g (Graph.)]
    (ocall g :setGraph #js{:nodesep 50 :ranksep 50 :edgesep 50 :rankdir "tb" :align "dl" :marginx (:left min-margins) :marginy (:top min-margins)})
    (doseq [{:keys [id]} nodes]
      (ocall g :setNode id (clj->js (assoc (get node-dimensions id) :label id))))
    (doseq [{:keys [from to type]} edges] 
      (ocall g :setEdge from to #js{}))
    (ocall dagre :layout g)
    (let [og (ocall g :graph)]
      (-> {:nodes      (into {} (map (fn [n] [n (js->clj (ocall g :node n) :keywordize-keys true)]) (ocall g :nodes)))
           :edges      (into {} (map (fn [e] 
                                        (let [id [(oget e :v) (oget e :w)]
                                              edge (js->clj (ocall g :edge e) :keywordize-keys true)]
                                          [id (assoc edge :node-ids (set id))])) (ocall g :edges)))
           :dimensions {:width  (oget og :width)
                        :height (oget og :height)}}
          force-margins))))

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
