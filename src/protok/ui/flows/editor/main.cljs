(ns protok.ui.flows.editor.main
  (:require [protok.react :refer [resize-detector pathline]]
            [keechma.ui-component :as ui]
            [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd <comp-swap!]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.logging :as l]
            [keechma.toolbox.ui :refer [route>]]
            [clojure.string :as str]
            [protok.styles.colors :refer [colors]]
            [protok.domain.project-files :as project-files]))

(def edge-color (colors :neutral-6))

(defn px [val]
  (str val "px"))

(defn calculate-svg-node-position [p]
  (when p
    (let [{:keys [width height x y]} p]
      {:y (px (- y (/ height 2)))
       :x (px (- x (/ width 2)))})))

(defn render-resize-detector [ctx node]
  [resize-detector
   {:handle-width true
    :handle-height true
    :on-resize #(<comp-swap! ctx assoc-in [:node-dimensions (:id node)] {:width %1 :height %2})}])

(defn render-edit-button [ctx node]
  (let [route (route> ctx)]
    [:div
     [:a {:href (ui/url ctx (assoc route :node-id (:id node)))} "EDIT"]]))

(defelement -temp-inner-node-wrap
  :class [:p2 :rounded])

(defelement -screen-wrap
  :class [:bg-white :rounded :sh2]
  :style [{:min-width "300px"}
          [:img {:width "300px"}]])

(defn render-screen [ctx state node]
  (let [pf-getter (:projectFile node)
        pf (when pf-getter (pf-getter))]
    [-screen-wrap 
     [render-resize-detector ctx node]
     (if pf
       [:div
        [:img {:src (project-files/url pf)}]
        [:div.p1
         [render-edit-button ctx node]]]
       [:div.p2
        "SCREEN " (:name node)
        [render-edit-button ctx node]])]))

(defelement -event-wrap
  :class [:bg-white]
  :style [{:width "300px"}])

(defn render-event [ctx state node]
   [-event-wrap
    [render-resize-detector ctx node]
    [-temp-inner-node-wrap
     (:name node)
     [render-edit-button ctx node]]])

(defelement -switch-wrap
  :class [:bg-white]
  :style [{:width "300px"}])

(defn render-switch [ctx state node]
  (let [os (:options node)]
    [-switch-wrap
     [render-resize-detector ctx node]
     [-temp-inner-node-wrap
      (:name node)
      [:ul.my2
       (map
        (fn [o]
          [:li {:key (:id o)} (:name o)])
        os)]
      [render-edit-button ctx node]]]))

(defelement -flow-ref-wrap
  :class [:bg-white]
  :style [{:width "300px"}])

(defn render-flow-ref [ctx state node]
  [-flow-ref-wrap 
   [render-resize-detector ctx node] 
   [-temp-inner-node-wrap
    "FLOW REF"
    [render-edit-button ctx node]]])

(defn render-svg-node [ctx state node]
  (let [node-layout (get-in state [:layout :layout :nodes (:id node)])]
    [:foreignObject (merge {:width  (px (or (:width node-layout) 0))
                            :height (px (or (:height node-layout) 0))}
                           (calculate-svg-node-position node-layout))
     [:div
      {:style (merge {:visibility (if node-layout "visible" "hidden")})}
      (case (:type node)
        "SCREEN"   [render-screen ctx state node]
        "EVENT"    [render-event ctx state node]
        "SWITCH"   [render-switch ctx state node]
        "FLOW_REF" [render-flow-ref ctx state node]
        nil)]]))

(defn render-edge [id edge]
  [pathline {:points (:points edge)
             :stroke-width 2 
             :stroke edge-color 
             :fill "none"
             :marker-end "url(#edge-arrow)"
             :marker-start "url(#edge-circle)"
             :r 20}])

(defn render-svg [ctx state]
  (let [layout (get-in state [:layout :layout])
        edges (:edges layout)
        width (or (get-in layout [:dimensions :width]) 0)
        height (or (get-in layout [:dimensions :height]) 0)
        nodes-getter (get-in state [:flow :flowNodes])
        nodes        (nodes-getter)]
    [:svg.mx-auto.block {:viewBox (str "0 0 " width " " height) :width width :height height}
     [:defs
      [:marker {:id "edge-arrow"
                :markerWidth 10
                :markerHeight 10
                :refX 8
                :refY 5
                :orient "auto"
                :viewBox "0 0 20 20"}
       [:path {:d "M0,0 L10,5 L0,10 z"
               :fill edge-color}]]
      [:marker {:id "edge-circle"
                :markerWidth 10
                :markerHeight 10
                :refX 5
                :refY 5
                :viewBox "0 0 20 20"}
       [:circle {:r 4 :cx 5 :cy 5 :fill edge-color}]]]
     (map (fn [n] 
            ^{:key (:id n)}
            [render-svg-node ctx state n])
          nodes)
     (map
      (fn [[id e]]
        ^{:key id}
        [render-edge id e])
      edges)]))

(defelement -sidebar-wrap
  :class [:absolute :right-0 :bottom-0 :top-0 :bg-white :bwl1 :bwt1 :bd-neutral-7 :overflow-auto]
  :style [{:width "38.2%"}])

(defn render-sidebar [ctx state]
  [-sidebar-wrap
   [(ui/component ctx :flows/node-form)]])

(defelement -wrap
  :class [:absolute :top-0 :left-0 :right-0 :bottom-0 :overflow-auto :bwt1 :bd-neutral-7]
  :style [{:left "90px"}
          [:&.no-sidebar
           {:right 0}]
          [:&.sidebar
           {:width "61.8%"}]])

(defn render [ctx state]
  (let [route        (route> ctx)
        nodes-getter (get-in state [:flow :flowNodes])
        nodes        (nodes-getter)
        layout       (get-in state [:layout :layout])
        has-sidebar? (:node-id route)
        edges        (:edges layout)]
    ;;(l/pp "LAYOUT" layout)
    [:<>
     [-wrap {:class (if has-sidebar? :sidebar :no-sidebar)}
      [render-svg ctx state]]
     (when has-sidebar?
       [render-sidebar ctx state])]))
