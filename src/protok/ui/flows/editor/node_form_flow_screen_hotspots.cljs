(ns protok.ui.flows.editor.node-form-flow-screen-hotspots
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.css.core :refer [defelement]]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.react :refer [resize-detector rnd]]
            [reagent.core :as r]
            [oops.core :refer [ocall oget]]
            [protok.styles.colors :refer [colors]]))

(defn px [v]
  (str v "px"))

(defelement -wrap
  :class [:absolute :top-0 :bottom-0 :left-0 :right-0]
  :style [{:transition "all 0.15s ease-in"}
          [:&.inactive {:opacity 0
                        :transform "scale(1.02)"
                        }
           [:&:hover {:opacity 1
                      :transform "scale(1)"
                      }]]])

(defelement -hotspot
  :class [:absolute :bw2]
  :style [[:&.active
           {:background-color "rgba(25,146,212,0.5)"
            :border-color (colors :blue-3)}]
          [:&.inactive 
           {:border-color (colors :neutral-5)}]])

(defelement -hotspot-index
  :class [:absolute :c-white :center :fs0 :bold]
  :style [{:width "16px"
           :height "16px"
           :border-radius "8px"
           :line-height "16px"
           :margin-top "-8px"
           :margin-left "-8px"
           :left "50%"}
          [:&.active {:background (colors :blue-3)}]
          [:&.inactive {:background (colors :neutral-5)}]])

(defn drag-handler [ctx form-props idx parent-dimensions drag]
  (let [top (oget drag :y)
        left (oget drag :x)
        hotspot (forms-ui/value-in> ctx form-props [:hotspots idx])
        updated-hotspot (-> hotspot
                            (assoc-in [:coordinates :top] (/ top (:height parent-dimensions)))
                            (assoc-in [:coordinates :left] (/ left (:width parent-dimensions))))]
    (forms-ui/<set-value ctx form-props [:hotspots idx] updated-hotspot)))

(defn resize-handler [ctx form-props idx parent-dimensions el drag]
  (let [parent-height (:height parent-dimensions)
        parent-width (:width parent-dimensions)
        top (oget drag :y)
        left (oget drag :x)
        height (oget el :offsetHeight)
        width (oget el :offsetWidth)
        hotspot (forms-ui/value-in> ctx form-props [:hotspots idx])
        updated-hotspot (-> hotspot
                            (assoc-in [:coordinates :top] (/ top parent-height))
                            (assoc-in [:coordinates :left] (/ left parent-width))
                            (assoc-in [:dimensions :width] (/ width parent-width))
                            (assoc-in [:dimensions :height] (/ height parent-height)))]
    (forms-ui/<set-value ctx form-props [:hotspots idx] updated-hotspot)))

(defn render-editable-hotspot [ctx form-props idx hotspot el-dimensions]
  (let [width     (or (get-in hotspot [:dimensions :width]) 0.5)
        height    (or (get-in hotspot [:dimensions :height]) 0.1)
        top       (or (get-in hotspot [:coordinates :top]) 0.1)
        left      (or (get-in hotspot [:coordinates :left]) 0.1)
        el-width  (:width el-dimensions)
        el-height (:height el-dimensions)
        real-left (* left el-width)
        real-top  (* top el-height)
        real-width (* width el-width)
        real-height (* height el-height)]
    [rnd {:size {:width real-width
                 :height real-height}
          :position {:x real-left
                     :y real-top}
          :bounds "parent"
          :on-resize-stop #(resize-handler ctx form-props idx el-dimensions %3 %5)
          :on-resize #(resize-handler ctx form-props idx el-dimensions %3 %5)
          :on-drag-stop #(drag-handler ctx form-props idx el-dimensions %2)
          :on-drag #(drag-handler ctx form-props idx el-dimensions %2)}
     [-hotspot
      {:style {:width "100%" :height "100%"}
       :class "active"}
      [-hotspot-index {:class "active"} (inc idx)]]]))

(defn render-editable-hotspots [ctx form-props form-state]
  (let [el-dimensions$ (r/atom nil)]
    (fn [ctx form-props form-state]
      (let [hotspots (get-in form-state [:data :hotspots])
            el-dimensions @el-dimensions$]
        [-wrap
         [resize-detector
          {:handle-width true
           :handle-height true
           :on-resize #(reset! el-dimensions$ {:width %1 :height %2})}]
         (map-indexed
          (fn [idx hotspot]
            ^{:key idx}
            [render-editable-hotspot ctx form-props idx hotspot el-dimensions])
          hotspots)]))))

(defn render-hotspot [ctx idx hotspot el-dimensions]
  (let [width     (or (get-in hotspot [:dimensions :width]) 0.5)
        height    (or (get-in hotspot [:dimensions :height]) 0.1)
        top       (or (get-in hotspot [:coordinates :top]) 0.1)
        left      (or (get-in hotspot [:coordinates :left]) 0.1)
        el-width  (:width el-dimensions)
        el-height (:height el-dimensions)
        real-left (* left el-width)
        real-top  (* top el-height)
        real-width (* width el-width)
        real-height (* height el-height)]
    [-hotspot
     {:class "inactive"
      :style {:top (px real-top)
              :left (px real-left)
              :width (px real-width)
              :height (px real-height)}}
     [-hotspot-index {:class "inactive"} (inc idx)]]))

(defn render-hotspots [ctx node]
   (let [el-dimensions$ (r/atom nil)]
     (fn [ctx node]
       (let [hotspots (:hotspots node) 
             el-dimensions @el-dimensions$]
         [-wrap {:class "inactive"}
          [resize-detector
           {:handle-width true
            :handle-height true
            :on-resize #(reset! el-dimensions$ {:width %1 :height %2})}]
          (map-indexed
           (fn [idx hotspot]
             ^{:key idx}
             [render-hotspot ctx  idx hotspot el-dimensions])
           hotspots)]))))

(defn render [ctx node]
  (let [form-props [:flow-screen (:id node)]
        form-state (forms-ui/form-state> ctx form-props)]
    (if form-state
      [render-editable-hotspots ctx form-props form-state]
      [render-hotspots ctx node])))

(def component
  (ui/constructor {:renderer render}))
