(ns protok.ui.flows.editor.main
  (:require [protok.react :refer [resize-detector pathline]]
            [keechma.ui-component :as ui]
            [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd <comp-swap!]]
            [keechma.toolbox.util :refer [class-names]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.logging :as l]
            [keechma.toolbox.ui :refer [route>]]
            [clojure.string :as str]
            [protok.styles.colors :refer [colors]]
            [protok.ui.flows.editor.flow-screen :as flow-screen]
            [protok.ui.flows.editor.flow-event :as flow-event]
            [protok.ui.flows.editor.flow-switch :as flow-switch]
            [protok.ui.flows.editor.flow-flow-ref :as flow-flow-ref]
            [protok.ui.flows.editor.shared :refer [node-type-icon]]
            [protok.ui.components.buttons :as buttons]
            [protok.ui.flows.editor.shared :refer [flow-node-types]]))

(def graph-options
  [{:label   "Direction"
    :id      :direction
    :options [{:label "Horizontal"
               :value :horizontal}
              {:label "Vertical"
               :value :vertical}]}
   {:label "Zoom"
    :id :zoom
    :options [{:label "100%"
               :value :actual}
              {:label "Fit"
               :value :fit}]}])

(def background-pattern
  "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='100' height='100' viewBox='0 0 100 100'%3E%3Cg fill-rule='evenodd'%3E%3Cg fill='%239C92AC' fill-opacity='0.2'%3E%3Cpath opacity='0.5' d='M96 95h4v1h-4v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4h-9v4h-1v-4H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15v-9H0v-1h15V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h9V0h1v15h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9h4v1h-4v9zm-1 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm9-10v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-10 0v-9h-9v9h9zm-9-10h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9zm10 0h9v-9h-9v9z'/%3E%3Cpath d='M6 5V0H5v5H0v1h5v94h1V6h94V5H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")

(def sidebar-width "600px")

(def edge-colors
  {:default  (colors :neutral-6)
   :inactive (colors :neutral-7)
   :active   (colors :blue-3)})

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

(defelement -node-header-wrap
  :class [:p1 :flex :flex-row :justify-between :items-center :bg-neutral-8])

(defelement -node-header-link
  :tag :a
  :class [:c-neutral-4 :c-h-white :bg-neutral-9 :bg-h-blue-4 :pill :text-decoration-none :bold :fs0]
  :style [{:text-transform "uppercase"
           :padding "3px 12px"
           :line-height 1
           :letter-spacing "0.02em"}])

(defelement -node-header-icons-wrap
  :style [[:svg {:fill (colors :neutral-4)
                 :height "18px"
                 :display "block"}]])

(defn get-header-link-props [ctx node]
  (let [route        (route> ctx)
        id           (:id node)
        active-node? (= id (:node-id route))
        editing?     (= "edit" (:subpage route))]
    (cond
      active-node? {:label "Close" :url (dissoc route :node-id)}
      editing?     {:label "Edit" :url (assoc route :node-id id)}
      :else        {:label "Details" :url (assoc route :node-id id)})))

(defn render-node-header [ctx node]
  [-node-header-wrap
   [-node-header-icons-wrap
    (node-type-icon (:type node))]
   (when-let [header-link-props (get-header-link-props ctx node)]
     [-node-header-link
      {:href (ui/url ctx (:url header-link-props))}
      (:label header-link-props)])])

(defn get-active-nodes [state]
  (let [edges (get-in state [:layout :layout :edges])
        active-node-id (:active-node-id state)]
    (when active-node-id
      (->> (map 
            (fn [[_ {:keys [node-ids]}]]
              (when (contains? node-ids active-node-id) 
                node-ids))
            edges)
           (filter (complement nil?))
           (apply concat)
           set))))

(defelement -node-wrap
  :class [:bg-white :rounded :overflow-hidden]
  :style [{:width "308px"}])

(defelement -inner-node-wrap
  :class [:bw2 :bd-white]
  :style [{:padding "2px"}
          [:&.inactive {:opacity 0.4}]
          [:&.active {:border-color (edge-colors :active)}]])

(defn render-node [ctx state node node-component]
  (let [active-nodes (get-active-nodes state)
        active-node-id (:active-node-id state)
        node-id (:id node)
        active-node? (= node-id active-node-id)
        activated-node? (and (seq active-nodes) (contains? active-nodes node-id))
        inactive-node? (and (seq active-nodes) (not (contains? active-nodes node-id)))]
    [-node-wrap 
     {:class (class-names {:sh2 (or (not (seq active-nodes)) (and (not active-node?) activated-node?))
                           :sh4 active-node?})}
     [render-resize-detector ctx node] 
     [-inner-node-wrap
      {:class (class-names {:inactive inactive-node?
                            :active (= node-id active-node-id)})}
      [render-node-header ctx node]
      [node-component ctx state node]]]))

(defn render-svg-node [ctx state node]
  (let [node-layout    (get-in state [:layout :layout :nodes (:id node)])
        node-component (case (:type node)
                         "SCREEN"   flow-screen/render 
                         "EVENT"    flow-event/render
                         "SWITCH"   flow-switch/render
                         "FLOW_REF" flow-flow-ref/render
                         nil)]
    [:foreignObject (merge {:width  (px (or (:width node-layout) 0))
                            :height (px (or (:height node-layout) 0))}
                           (calculate-svg-node-position node-layout))
     [:div 
      {:style {:visibility (if node-layout "visible" "hidden")}}
      [render-node ctx state node node-component]]]))

(defn render-edge [id edge edge-color]
  (let [marker-start (if-let [index (:index edge)]
                       (str "edge-circle-" index)
                       "edge-circle")]
    (when-let [points (seq (:points edge))]
      [pathline {:points points
                 :stroke-width 2 
                 :stroke edge-color
                 :fill "none"
                 :marker-end "url(#edge-arrow)"
                 :marker-start (str "url(#" marker-start ")")
                 :r 3
                 :stroke-linecap "round"}])))

(defn render-active-edge [id edge]
  (let [marker-start (if-let [index (:index edge)]
                       (str "active-edge-circle-" index)
                       "active-edge-circle")]
    (when-let [points (seq (:points edge))]
      [pathline {:points points
                 :stroke-width 2 
                 :stroke (edge-colors :active) 
                 :fill "none"
                 :marker-end "url(#active-edge-arrow)"
                 :marker-start (str "url(#" marker-start ")")
                 :r 3
                 :stroke-linecap "round"}])))

(defn render-edge-bg [id edge edge-color]
  (when-let [points (seq (:points edge))]
    [pathline {:points points
               :stroke-width 2 
               :stroke edge-color 
               :fill "none"
               :r 3}]))

(defn render-markers
  ([max-edge-index marker-color] (render-markers max-edge-index marker-color "edge"))
  ([max-edge-index marker-color prefix]
   [:<>
    [:marker {:id (str prefix "-arrow")
              :markerWidth 10
              :markerHeight 10
              :refX 4
              :refY 5
              :orient "auto"
              :viewBox "0 0 20 20"}
     [:path {:d "M0,0 L10,5 L0,10 z"
             :fill marker-color}]]
    [:marker {:id (str prefix "-circle")
              :markerWidth 10
              :markerHeight 10
              :refX 5
              :refY 5
              :viewBox "0 0 20 20"}
     [:circle {:r 4 :cx 5 :cy 5 :fill marker-color}]]
    (map
     (fn [idx]
       ^{:key idx}
       [:marker {:id (str prefix "-circle-" idx)
                 :markerWidth 18
                 :markerHeight 18
                 :refX 10
                 :refY 10
                 :viewBox "0 0 39 39"}
        [:circle {:r 9 :cx 11 :cy 10 :stroke-width 2 :stroke marker-color :fill "white"}]
        [:text {:x 11 :y 14 :fill marker-color :font-size "13" :font-weight "bold" :width 33 :text-anchor "middle"} (inc idx)]])
     (range (inc max-edge-index)))]))

(defn render-svg [ctx state]
  (let [layout         (get-in state [:layout :layout])
        edges          (:edges layout)
        width          (or (get-in layout [:dimensions :width]) 0)
        height         (or (get-in layout [:dimensions :height]) 0)
        nodes-getter   (get-in state [:flow :flowNodes])
        nodes          (nodes-getter)
        active-node-id (:active-node-id state)
        active-edge-id (:active-edge-id state)
        active-edges   (cond 
                         active-node-id (filter (fn [[_ e]] (= (first (:id e)) active-node-id)) edges)
                         active-edge-id (filter (complement nil?) [[active-edge-id (get edges active-edge-id)]])
                         :else          nil)
        edge-color     (if (or active-node-id (seq active-edges)) (edge-colors :inactive) (edge-colors :default))
        max-edge-index (:max-edge-index layout)]

    [:svg.mx-auto.block {:viewBox (str "0 0 " width " " height) :width width :height height}
     [:defs
      [render-markers max-edge-index edge-color]
      [render-markers max-edge-index (edge-colors :active) "active-edge"]] 
     (map (fn [n] 
            ^{:key (:id n)}
            [render-svg-node ctx state n])
          nodes) 
     (map
      (fn [[id e]]
        ^{:key id}
        [render-edge id e edge-color])
      edges)
     (map
      (fn [[id e]]
        ^{:key id}
        [render-active-edge id e])
      active-edges)]))

(defelement -sidebar-wrap
  :class [:absolute :right-0 :bottom-0 :top-0 :bg-white :bwl1 :bwt1 :bd-neutral-7 :overflow-auto]
  :style [{:width sidebar-width}])

(defn render-sidebar [ctx state]
  (let [editing? (= "edit" (:subpage (route> ctx)))]
    [-sidebar-wrap
     (if editing?
       [(ui/component ctx :flows/node-form)]
       [(ui/component ctx :flows/node-details)])]))

(defelement -wrap
  :style [[:.protok_ui_flows_editor_main--editor-wrap
           :.protok_ui_flows_editor_main--menu-wrap
           {:right 0}]
          [:&.has-sidebar
           [:.protok_ui_flows_editor_main--editor-wrap
            :.protok_ui_flows_editor_main--menu-wrap
            {:right sidebar-width}]]])

(defelement -editor-wrap
  :class [:absolute :left-0 :right-0 :bottom-0 :overflow-auto :bwt1 :bd-neutral-7]
  :style [{:top "50px"
           :background-image (str "url(\"" background-pattern "\")")
           :background-position "-6px -6px"
           :transform "translate3d(0,0,0)"}
          [:&.zoom-fit
           [:svg {:max-width "100%"
                  :max-height "100%"}]]])

(defn render-editor [ctx state]
  (let [editor-wrap-id (str (gensym "editor-wrap"))]
    (fn [ctx state]
      [-editor-wrap
       {:ref #(<comp-swap! ctx assoc :editor-el editor-wrap-id)
        :id editor-wrap-id
        :class (class-names {:zoom-fit (= :fit (get-in state [:options :zoom]))})} 
       [render-svg ctx state]])))

(defelement -menu-wrap
  :class [:absolute :top-0 :left-0 :bd-neutral-7 :bwt1 :flex :flex-row :justify-center :items-center]
  :style [{:height "50px"}])

(defelement -menu-group-wrap
  :class [:flex :flex-row :items-center :mx2])

(defelement -menu-group-label
  :class [:c-neutral-4 :fs0 :bold :mr1]
  :style [{:text-transform "uppercase"}])

(defelement -menu-group-buttons-wrap
  :style [{:padding-left "1px"}])

(defelement -menu-group-button
  :tag :button
  :class [:bg-neutral-9 :bd-neutral-7 :bw1 :fs0 :c-neutral-3 :center :pointer :relative]
  :style [{:padding "0px 10px"
           :margin-left "-1px"
           :height "24px"
           :min-width "5rem"
           :outline "none"}
          [:&:first-child
           {:border-top-left-radius "3px"
            :border-bottom-left-radius "3px"}]
          [:&:last-child
           {:border-top-right-radius "3px"
            :border-bottom-right-radius "3px"}]
          [:&:hover
           {:background-color (colors :neutral-6)
            :border-color (colors :netural-6)
            :color (colors :white)}]
          [:&.active
           {:background-color (colors :blue-4)
            :border-color (colors :blue-4)
            :color (colors :white)
            :z-index 1}]])

(defn render-editor-menu [ctx state]
  (let [active-graph-options (:options state)]
    [-menu-wrap
     [-menu-group-wrap
      [-menu-group-label "Add new"]
      [-menu-group-buttons-wrap
       (map 
        (fn [n]
          (let [node-type (:type n)]
            ^{:key node-type}
            [-menu-group-button
             {:on-click #(<comp-cmd ctx :create-node [node-type true])}
             (:name n)]))
        flow-node-types)]]
     [-menu-group-wrap
      [-menu-group-buttons-wrap
       [-menu-group-button "Add multiple screens"]]]
     (map 
      (fn [g]
        (let [option-id (:id g)]
          ^{:key option-id}
          [-menu-group-wrap
           (when-let [label (:label g)]
             [-menu-group-label label])
           [-menu-group-buttons-wrap
            (map
             (fn [o]
               (let [option-value (:value o)]
                 ^{:key option-value}
                 [-menu-group-button
                  {:on-click #(<comp-swap! ctx assoc-in [:options option-id] option-value)
                   :class (class-names {:active (= option-value (get active-graph-options option-id))})}
                  (:label o)]))
             (:options g))]]))
      graph-options)]))

(defn render-viewer-menu [ctx state]
  (let [active-graph-options (:options state)]
    [-menu-wrap 
     (map 
      (fn [g]
        (let [option-id (:id g)]
          ^{:key option-id}
          [-menu-group-wrap
           (when-let [label (:label g)]
             [-menu-group-label label])
           [-menu-group-buttons-wrap
            (map
             (fn [o]
               (let [option-value (:value o)]
                 ^{:key option-value}
                 [-menu-group-button
                  {:on-click #(<comp-swap! ctx assoc-in [:options option-id] option-value)
                   :class (class-names {:active (= option-value (get active-graph-options option-id))})}
                  (:label o)]))
             (:options g))]]))
      graph-options)]))

(defn render-menu [ctx state]
  (if (= "edit" (:subpage (route> ctx)))
    [render-editor-menu ctx state]
    [render-viewer-menu ctx state]))


(defn render [ctx state]
  (let [route        (route> ctx)
        has-sidebar? (:node-id route)]
    ;;(l/pp "LAYOUT" layout)
    [-wrap {:class (when has-sidebar? :has-sidebar)}
     [render-menu ctx state]
     [render-editor ctx state]
     (when has-sidebar?
       [render-sidebar ctx state])]))
