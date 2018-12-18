(ns protok.ui.editor
  (:require [keechma.toolbox.entangled.ui :as entangled-ui :refer [<comp-cmd]]
            [protok.ui.editor.actions :refer [actions]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [reagent.core :as r]
            [protok.ui.editor.states.shared :refer [selected-multiple?]]))

(defelement -wrap
  :class [:flex]
  :style {:height "100vh"
          :width "100vw"
          :flex-direction "column"})

(defelement -editor
  :class [:relative :flex]
  :style [{:flex-grow "1"
           :background-color "#fefefe"
           :background-position "11px 11px"
           :background-image "url(/images/grid.png)"
           :overflow "hidden"}])

(defelement -card
  :class [:absolute]
  :style [[:&.selected {:box-shadow "0 0 0 1px #2880E6"}]])

(defelement -selection-bounding-box
  :class [:absolute]
  :style [{:box-shadow "0 0 0 1px #2880E6"}])

(defelement -selection
  :class [:border :absolute]
  :style [{:border-color "#2880E6"
           :background-color "rgba(40,128,230,0.1)"}])

(defelement -toolbar-wrap
  :class [:flex :items-center :justify-center]
  :style {:height "40px"
          :background-color "#333"})

(def px-props #{:top :right :bottom :left :width :height})

(defn pixelize [props]
  (into {} (map (fn [[k v]] [k (if (contains? px-props k) (str v "px") v)]) props)))

(defn render-selection-bounding-box [state]
  (when-let [box (get-in state [:interaction :selected-bounding-box])]
    [-selection-bounding-box {:key :selection-bounding-box :class "selected" :style (pixelize box)}]))

(defn render-translate-selected-style [{:keys [translate-selected]}]
  (when translate-selected
    [:style {:type "text/css"}
     (str ".selected { transform: translate(" (:x translate-selected) "px," (:y translate-selected) "px)}")]))

(defn render-toolbar [ctx state-name state]
  [-toolbar-wrap
   (when (and (= :editor/selected state-name)
              (selected-multiple? state))
     [:div.p1
      [:button {:on-click #(<comp-cmd ctx :align :vertical-top)} "Align VT"]
      [:button {:on-click #(<comp-cmd ctx :align :vertical-center)} "Align VC"]
      [:button {:on-click #(<comp-cmd ctx :align :vertical-bottom)} "Align VB"]
      [:button {:on-click #(<comp-cmd ctx :align :vertical-spacing)} "Align VS"]
      [:button {:on-click #(<comp-cmd ctx :align :horizontal-left)} "Align HL"]
      [:button {:on-click #(<comp-cmd ctx :align :horizontal-center)} "Align HC"]
      [:button {:on-click #(<comp-cmd ctx :align :horizontal-right)} "Align HR"]
      [:button {:on-click #(<comp-cmd ctx :align :horizontal-spacing)} "Align HS"]])])

(defn render [ctx state]
  (let [ref$ (atom nil)]
    (r/create-class
     {:component-did-mount #(<comp-cmd ctx :register-element @ref$)
      :reagent-render
      (fn [ctx current]
        (let [state (:state current)
              state-name (:name current)
              interaction (:interaction state) 
              layers (get-in state [:document :layers])]
          [-wrap
           [render-toolbar ctx state-name state]
           [-editor {:ref #(reset! ref$ %)}
            (map
             (fn [l] 
               ^{:key (:id l)}
               [-card {:style (merge (pixelize (:rect l)) (:style l))
                       :class (when (contains? (:selected interaction) (:id l)) "selected")}])
             (vals (sort-by :order layers)))
            (when-let [selection (:selection interaction)]
              [-selection {:style (pixelize selection)}])
            [render-selection-bounding-box state]
            [render-translate-selected-style interaction]]]))})))

(defn state-provider [ctx local-state _]
  ;;(println (with-out-str (cljs.pprint/pprint local-state)))
  (println (get-in local-state [:current :name]))
  (:current local-state))

(def component
  (entangled-ui/constructor
   {:renderer render
    :state-provider state-provider}
   actions))
