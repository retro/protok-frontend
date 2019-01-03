(ns protok.ui.flows.editor.main
  (:require [protok.react :refer [resize-detector]]
            [keechma.ui-component :as ui]
            [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd <comp-swap!]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.logging :as l]
            [keechma.toolbox.ui :refer [route>]]
            [clojure.string :as str]))

(def graph-margin 50)

(defn px [val]
  (str val "px"))

(defn calculate-node-position [p]
  (when p
    (let [{:keys [width height x y]} p
          top (- y (/ height 2))
          left (- x (/ width 2))]
      {:top (px top)
       :left (px left)})))

(defn render-resize-detector [ctx node]
  [resize-detector
   {:handle-width true
    :handle-height true
    :on-resize #(<comp-swap! ctx assoc-in [:node-dimensions (:id node)] {:width %1 :height %2})}])

(defn render-edit-button [ctx node]
  (let [route (route> ctx)]
    [:div
     [:a {:href (ui/url ctx (assoc route :node-id (:id node)))} "EDIT"]]))

(defelement -screen-wrap
  :class [:bg-yellow-8 :bd-black :bw2 :rounded]
  :style [{:width "300px"}])

(defn render-screen [ctx state node]
  [-screen-wrap 
   [render-resize-detector ctx node]
   [:div.p1
    "SCREEN " (:name node)
    [render-edit-button ctx node]]])

(defelement -event-wrap
  :class [:bg-green-8 :bd-black :bw2 :rounded]
  :style [{:width "300px"}])

(defn render-event [ctx state node]
   [-event-wrap
    [render-resize-detector ctx node]
    [:div.p1
     "EVENT " (:name node)
     [render-edit-button ctx node]]])

(defelement -switch-wrap
  :class [:bg-blue-8 :bd-black :bw2 :rounded]
  :style [{:width "300px"}])

(defn render-switch [ctx state node]
   [-switch-wrap
    [render-resize-detector ctx node]
    [:div.p1
     "SWITCH " (:name node)
     [render-edit-button ctx node]]])

(defelement -flow-ref-wrap
  :class [:bg-red-8 :bd-black :bw2 :rounded]
  :style [{:width "300px"}])

(defn render-flow-ref [ctx state node]
  [-flow-ref-wrap 
   [render-resize-detector ctx node] 
   [:div.p1
    "FLOW REF"
    [render-edit-button ctx node]]])

(defelement -node-wrap
  :class [:absolute])

(defn render-node [ctx state node]
  (let [node-layout (get-in state [:layout :layout :nodes (:id node)])]
    [-node-wrap 
     {:style (merge {:visibility (if node-layout "visible" "hidden")}
                    (calculate-node-position node-layout))}
     (case (:type node)
       "SCREEN"   [render-screen ctx state node]
       "EVENT"    [render-event ctx state node]
       "SWITCH"   [render-switch ctx state node]
       "FLOW_REF" [render-flow-ref ctx state node]
       nil)]))

(defelement -point-start
  :class [:bw2 :bd-black :bg-white :absolute]
  :style [{:width "8px"
           :height "8px"
           :border-radius "4px"}])

(defn render-edge-start-point [point]
  [-point-start
   {:style {:top (px (- (:y point) 1))
            :left (px (- (:x point) 4))}}])

(defn render-edge-end-point [point]
  [-point-start
   {:style {:top (px (- (:y point) 4))
            :left (px (- (:x point) 4))}}])

(defelement -edges-wrap
  :class [:absolute]
  :style [{:top (px graph-margin)
           :left (px graph-margin)}])

(defn render-edge [points]
  (let [first-point (first points)
        rest-points (rest points)
        d (str "M " (:x first-point) " " (:y first-point) " "
               (str/join " " (map #(str "L " (:x %) " " (:y %)) rest-points)))]
    [:path {:d d
            :stroke-width 2
            :stroke "black"
            :fill "none"
            :marker-end "url(#edge-arrow)"}]))

(defn render-edges [ctx state]
  (let [layout (get-in state [:layout :layout])
        edges (:edges layout)
        width (get-in layout [:dimensions :width])
        height (get-in layout [:dimensions :height])]
    (when layout
      [-edges-wrap
       [:svg {:viewBox (str "0 0 " width " " height) :width width :height height}
        [:defs
         [:marker {:id "edge-arrow"
                   :markerWidth 10
                   :markerHeight 10
                   :refX 12
                   :refY 5
                   :orient "auto"
                   :viewBox "0 0 20 20"}
          [:path {:d "M0,0 L10,5 L0,10 z"
                  :fill "black"}]]]
        (map-indexed
         (fn [idx e]
           ^{:key idx}
           [render-edge e])
         edges)]])))

(defelement -sidebar-wrap
  :class [:absolute :right-0 :bottom-0 :top-0 :bg-white :bwl1 :bwt1 :bd-neutral-7 :overflow-auto]
  :style [{:width "38.2%"}])

(defn render-sidebar [ctx state]
  [-sidebar-wrap
   [(ui/component ctx :flows/node-form)]])

(defelement -wrap
  :class [:absolute :top-0 :left-0 :right-0 :bottom-0 :overflow-auto :bwt1 :bd-neutral-7]
  :style [[:&.no-sidebar
           {:right 0}]
          [:&.sidebar
           {:width "61.8%"}]])

(defelement -inner-wrap
  :class [:relative]
  :style [{:margin (px graph-margin)}])

(defn render [ctx state]
  (let [route        (route> ctx)
        nodes-getter (get-in state [:flow :flowNodes])
        nodes        (nodes-getter)
        layout       (get-in state [:layout :layout])
        has-sidebar? (:node-id route)
        edges        (:edges layout)]
    (l/pp layout)
    [:<>
     [-wrap {:class (if has-sidebar? :sidebar :no-sidebar)}
      [render-edges ctx state]
      [-inner-wrap
       {:style {:width  (px (+ graph-margin (get-in layout [:dimensions :width])))
                :height (px (get-in layout [:dimensions :height]))}}
       (map (fn [n] 
              ^{:key (:id n)}
              [render-node ctx state n])
            nodes)
       (map-indexed (fn [idx e] 
                      ^{:key idx}
                      [render-edge-start-point (first e)])
                    edges)
       (map-indexed (fn [idx e] 
                      ^{:key idx}
                      [render-edge-end-point (last e)])
                    edges)]]
     (when has-sidebar?
       [render-sidebar ctx state])]))
