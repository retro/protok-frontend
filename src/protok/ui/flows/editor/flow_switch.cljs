(ns protok.ui.flows.editor.flow-switch
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -wrap
  :class [:fs1 :c-neutral-2 :p1])

(defelement -option-item
  :tag :li
  :class [:relative]
  :style [{:padding-left "22px"
           :margin-bottom "5px"}])

(defelement -option-idx
  :class [:absolute :bg-neutral-6 :bold :c-white :center :fs0 :left-0]
  :style [{:height "16px"
           :width "16px"
           :border-radius "8px"
           :line-height "16px"
           :top "2px"}])

(defn render [ctx state node]
  (let [options (:options node)]
    [-wrap
     [:span.fs2 (:name node)]
     (when (seq options)
       [:ul.pt1
        (map-indexed
         (fn [idx {:keys [id name]}]
           ^{:key id}
           [-option-item
            [-option-idx (inc idx)]
            name])
         options)])]))
