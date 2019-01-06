(ns protok.ui.flows.editor.flow-screen
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.domain.project-files :as project-files]
            [keechma.ui-component :as ui]))

(defelement -wrap 
  :class [:fs2 :c-neutral-2])

(defelement -name-wrap
  :class [:p1 :bd-neutral-7]
  :style [{:margin-bottom "2px"}])

(defelement -img
  :tag :img
  :class [:block]
  :style [{:max-width "300px"}])

(defelement -img-wrap
  :class [:relative])

(defelement -img-progress-wrap
  :class [:absolute]
  :style [{:top "10px"
           :right "10px"
           :width "54px"
           :height "14px"
           :padding "2px"
           :border-radius "7px"
           :background-color "rgba(0,0,0,0.5)"}])

(defelement -img-progress-inner-wrap
  :class [:overflow-hidden]
  :style [{:width "50px"
           :height "10px"
           :border-radius "5px"
           :border "2px solid white"}])

(defelement -img-progress
  :style [{:background-color "white"
           :height "6px"}])

(defn render [ctx state node]
  (let [pf-getter (:projectFile node)
        pf (when pf-getter (pf-getter))
        progress (:protok/progress pf)]
    [-wrap
     [-name-wrap {:class (when pf :bwb1)} (:name node)]
     (when pf 
       [-img-wrap
        [-img {:src (project-files/url pf)}]
        (when (and progress (not= 1 progress))
          [-img-progress-wrap
           [-img-progress-inner-wrap
            [-img-progress {:style {:width (str (* 100 progress) "%")}}]]])
        [(ui/component ctx :flows/node-form-flow-screen-hotspots) node]])]))
