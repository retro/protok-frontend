(ns protok.ui.flows.editor.layout-calculator
  (:require [keechma.toolbox.entangled.shared :refer [get-state-app-db-path]]
            [keechma.toolbox.logging :as l]
            [oops.core :refer [oget ocall oset!]]
            [protok.edb :as edb]
            [com.rpl.specter :as s]
            [protok.settings :refer [elk-worker-path]]
            [promesa.core :as p]
            [clojure.set :as set]
            [elk]))

(declare a*-seq, next-a*-path, unseen?, step-factory, rpath, cmp-step)

(defn a*
  "A sequence of paths from `src` to `dest`, shortest first, within the supplied `graph`.
  If the graph is weighted, supply a `distance` function. To make use of A*, supply a 
  heuristic function. Otherwise performs like Dijkstra's algorithm."
  [graph src dest & {:keys [distance heuristic]}]
  (let [init-adjacent (sorted-set-by cmp-step {:node src :cost 0 :entered 0})]
    (a*-seq graph dest init-adjacent
            (or distance (constantly 1))
            (or heuristic (constantly 0)))))

(defn a*-seq
  "Construct a lazy sequence of calls to `next-a*-path`, returning the shortest path first."
  [graph dest adjacent distance heuristic]
  (lazy-seq
    (when-let [[path, adjacent'] (next-a*-path graph dest adjacent distance heuristic)]
      (cons path (a*-seq graph dest adjacent' distance heuristic)))))

(defn next-a*-path [graph dest adjacent f-cost f-heur]
  (when-let [{:keys [node] :as current} (first adjacent)]
    (let [path (rpath current)
          adjacent' (disj adjacent current)] ;; "pop" the current node
      (if (= node dest)
        [(reverse path), adjacent']
        (let [last-idx (or (:entered (last adjacent')) 0)
              factory (step-factory current last-idx f-cost f-heur dest)
              xform (comp (filter (partial unseen? path)) (map-indexed factory))
              adjacent'' (into adjacent' xform (get graph node))]
          (recur graph dest adjacent'' f-cost f-heur))))))

(defn unseen? [path node]
  (not-any? #{node} path))

(defn step-factory [parent last-insertion cost heur dest]
  (fn [insertion-idx node]
    {:parent parent
     :node node
     :entered (+ last-insertion (inc insertion-idx))
     :cost (+ (:cost parent) (cost (:node parent) node) (heur node dest))}))

(defn rpath [{:keys [node parent]}]
  (lazy-seq
    (cons node (when parent (rpath parent)))))

(defn cmp-step [step-a step-b]
  (let [cmp (compare (:cost step-a) (:cost step-b))]
    (if (zero? cmp)
      (compare (:entered step-a) (:entered step-b))
      cmp)))

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

(def node-types-priority
  {"SCREEN"   1
   "SWITCH"   2
   "EVENT"    3
   "FLOW_REF" 4})

(defn node-type-priority [node]
  (let [node-type (:type node)]
    (or (node-types-priority node-type) 999)))

(defn process-elk-node [n]
  (let [width (:width n)
        height (:height n)]
    (-> n
        (update :x #(+ % (/ width 2)))
        (update :y #(+ % (/ height 2))))))

(defn process-elk-edge [e]
  (let [node-ids (vec (js->clj (oget e :nodeIds)))]
    {:id node-ids 
     :node-ids (set node-ids)
     :index (oget e :index)
     :points (-> (map (fn [s]
                        [{:x (oget s :startPoint.x)
                          :y (oget s :startPoint.y)}
                         (map (fn [p] {:x (oget p :x) :y (oget p :y)}) (oget s :?bendPoints))
                         {:x (oget s :endPoint.x)
                          :y (oget s :endPoint.y)}]) (oget e :?sections))
                 flatten
                 vec)}))

(defn get-layout-options [options]
  {"elk.algorithm" "layered"
   "elk.layered.spacing.nodeNodeBetweenLayers" 25
   "elk.spacing.edgeNode" 25
   "elk.layered.spacing.edgeNodeBetweenLayers" 25
   "elk.layered.spacing.edgeEdgeBetweenLayers" 25
   "elk.direction" (if (= :horizontal (:direction options)) "RIGHT" "DOWN")
   "elk.edge.thickness" 2
   "elk.hierarchyHandling" "INCLUDE_CHILDREN"
   "elk.partitioning.activate" true
   "elk.spacing.portPort" 30
   "elk.layered.mergeEdges" true})

(defn entrypoint? [node]
  (:isEntrypoint node))

(defn get-entrypoint [nodes]
  (first (filter entrypoint? nodes)))

(defn from-port-id [e]
  (str "from_" (:from e) "_" (:to e)))

(defn to-port-id [e]
  (str "to_" (:to e) "_" (:from e)))

(defn edge->elk-edge [{:keys [from to index] :as e}]
  {:id (str from "/" to "/" (or index "_"))
   :sources [(from-port-id e)]
   :targets [(to-port-id e)]
   :index index
   :nodeIds [from to]})

(defn get-node-by-id [nodes id]
  (-> (group-by :id nodes)
      (get id)
      first))

(defn node->elk-node [nodes options node-dimensions edges {:keys [id priority] :as n}]
  (let [vertical? (= :vertical (:direction options))
        out-ports-side (if vertical? "SOUTH" "EAST")
        out-ports (map (fn [e]
                         (let [to (get-node-by-id nodes (:to e))
                               to-priority (:priority to)
                               side (cond
                                      (and vertical? (<= priority to-priority)) "SOUTH"
                                      (and (not vertical?) (<= priority to-priority)) "EAST"
                                      vertical? "NORTH"
                                      :else "WEST")]
                          {:id (from-port-id e) 
                           :layoutOptions {"elk.port.side" side}})) 
                       (sort-by :index (filter #(= id (:from %)) edges)))
        in-ports (map (fn [e]
                        (let [from (get-node-by-id nodes (:from e))
                              from-priority (:priority from)
                              side (cond
                                     (and vertical? (<= priority from-priority)) "EAST"
                                     (and (not vertical?) (<= priority from-priority)) "SOUTH"
                                     vertical? "NORTH"
                                     :else "WEST")]
                          {:id (to-port-id e) 
                           :layoutOptions {"elk.port.side" side}})) 
                      (filter #(= id (:to %)) edges))
        ports (concat out-ports in-ports)
        ports' (map-indexed (fn [idx p] (assoc-in p [:layoutOptions "elk.port.index"] idx)) (if vertical? (reverse ports) ports))]
    (-> (get node-dimensions id)
        (assoc :id id
               :ports ports'
               :layoutOptions {"elk.portAlignment.default" "CENTER"
                               "elk.portConstraints" "FIXED_ORDER"
                               "elk.partitioning.partition" priority
                               "elk.priority" (node-type-priority n)}))))

(defn adjust-coordinates [{:keys [x y]} nodes]
  (map 
   (fn [n]
     (-> n
         (update :x + x)
         (update :y + y)))
   nodes))

(defn get-nodes [parent]
  (let [parent-x (oget parent :x)
        parent-y (oget parent :y)]
    (->> (js->clj (oget parent :children) :keywordize-keys true)
         (adjust-coordinates {:x parent-x :y parent-y})
         (map process-elk-node))))

(defn adjust-edge-coordinates [coordinates edge]
  (update edge :points #(adjust-coordinates coordinates %)))

(defn get-edges [parent]
  (let [edges (vec (oget parent :edges))
        parent-x (oget parent :x)
        parent-y (oget parent :y)] 
    (->> edges
         (map process-elk-edge)
         (map #(adjust-edge-coordinates {:x parent-x :y parent-y} %)))))

(defn elk-calculate [options nodes edges node-dimensions]
  (let [g {:id "root"
           :layoutOptions (get-layout-options options)
           :children (map #(node->elk-node nodes options node-dimensions edges %) nodes)
           :edges (map edge->elk-edge edges)}
        e (elk. #js{:workerUrl elk-worker-path})]
    (->> (ocall e :layout (clj->js g))
         (p/map (fn [res]
                  (let [edges (vec (oget res :edges))]
                    (-> {:nodes (into {} (map (fn [n] [(:id n) n]) (get-nodes res)))
                         :edges (into {} (map (fn [e] [(:id e) e]) (concat (get-edges res))))
                         :dimensions {:height (+ (:top min-margins) (oget res :height))
                                      :width (+ (:left min-margins) (oget res :width))}}
                        add-max-edge-index
                        force-margins)))))))

(defn sort-nodes [nodes edges]
  (let [entrypoint (or (get-entrypoint nodes) (first nodes))
        entrypoint-id (:id entrypoint)
        graph (-> (reduce (fn [acc {:keys [from to]}] 
                            (assoc acc to (set/union (acc to) #{from})))
                          {} edges))]
    (->> nodes
         (map (fn [n]
                (let [priority (if (= n entrypoint) 1 (count (first (a* graph (:id n) entrypoint-id))))]
                  (assoc n :priority priority))))
         (sort-by :id)
         reverse)))

(defn get-layout [app-db ctx]
  (let [state-path (get-state-app-db-path ctx)
        state (get-in app-db state-path)
        options (:options state)
        node-dimensions (:node-dimensions state)
        flow (edb/get-named-item app-db :flow :current)
        nodes-getter (:flowNodes flow)
        nodes (process-nodes options (nodes-getter))
        edges (extract-all-edges options nodes)
        layout-id (hash [(map (fn [n] [(:id n) (:isEntrypoint n)]) nodes) edges node-dimensions options])]
    (cond
      (= layout-id (get-in state [:layout :id])) nil
      (not (all-nodes-have-dimensions? nodes node-dimensions)) nil
      :else (->> (elk-calculate options (sort-nodes nodes edges) edges node-dimensions)
                 (p/map (fn [l] {:id layout-id :layout l}))))))
