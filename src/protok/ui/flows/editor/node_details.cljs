(ns protok.ui.flows.editor.node-details
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> route>]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.icons :refer [icon]]
            [protok.styles.colors :refer [colors]]
            [protok.react :refer [markdown]]))

(defelement -wrap
  :class [:p2])

(defelement -title
  :class [:fs4 :c-neutral-2 :mb2])

(defelement -title-wrap
  :class [:flex :justify-between])

(defelement -close-link
  :tag :a
  :class [:block]
  :style [[:svg {:fill (colors :neutral-4)}]
          [:&:hover [:svg {:fill (colors :blue-4)}]]])

(defn flow-ref? [node]
  (= "FLOW_REF" (:type node)))

(defn get-title [node]
  (if (flow-ref? node)
    "Flow Ref"
    (:name node)))

(defn render-flow-ref [ctx node]
  [:div.c-neutral-2 
   "Go to flow: "
   [:a.c-blue-3 {:href (ui/url ctx {:page "flows" :subpage "view" :id (get-in node [:targetFlow :id])})}
    (get-in node [:targetFlow :name])]])

(defelement -markdown-wrap
  :class [:c-neutral-2]
  :style [[:h1 {:font-size "1.25rem"}]
          [:h2 {:font-size "1.20rem"}]
          [:h3 {:font-size "1.15rem"}]
          [:h4 {:font-size "1.10rem"}]
          [:h5 {:font-size "1.05rem"}]
          [:h6 {:font-size "1rem"}]
          [:p :blockquote :ul :ol :dl :table :pre
           {:margin "1rem 0"}]
          [:em :i {:font-style "italic"}]
          [:strong :b {:font-weight "bold"}]
          [:ul 
           {:list-style "disc outside none"
            :padding-left "1.5rem"}]
          [:hr
           {:height "2px"
            :border 0
            :color (colors :neutral-8)
            :background-color (colors :neutral-8)}]
          [:code :pre
           {:border-radius "3px"
            :background (colors :neutral-9)
            :font-size "0.9rem"
            :font-family "'SFMono-Regular', Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New', monospace"}]
          [:code
           {:border (str "1px solid " (colors :neutral-8))
            :margin "0 2px"
            :padding "0 5px"}]
          [:pre
           {:border (str "1px solid " (colors :neutral-8))
            :line-height "1.25em"
            :overflow "auto"
            :padding "6px 10px"}
           [:code
            {:border 0
             :margin 0
             :padding 0}]]
          [:a {:color (colors :blue-3)}
           [:&:hover {:text-decoration "none"}]]])

(defn render-node [ctx node]
  [-markdown-wrap [markdown (:description node)]])

(defn render [ctx]
  (let [node (sub> ctx :current-flow-node)
        route (route> ctx)]
   [-wrap 
    [-title-wrap
     [-title (get-title node)] 
     [-close-link
      {:href (ui/url ctx (dissoc route :node-id))} (icon :close)]]
    (if (flow-ref? node)
      [render-flow-ref ctx node]
      [render-node ctx node])]))

(def component
  (ui/constructor
   {:renderer render
    :subscription-deps [:current-flow-node]}))
