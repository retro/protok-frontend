(ns protok.ui.components.data-table
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.util :refer [class-names]]
            [clojure.string :as str]))

(defelement -table
  :tag :table
  :class [:w100p])

(defelement -thead-tr
  :tag :tr)

(defelement -th
  :tag :th
  :class [:left-align :py2 :bwb2 :bdb-neutral-7 :bold :c-neutral-5 :bg-neutral-9])

(defelement -tbody-tr
  :tag :tr
  :class [:bg-white :bg-h-yellow-9]
  :style [[:.on-row-hover-reveal
           [:.content
            {:visibility "hidden"}]]
          [:&:hover
           [:.on-row-hover-reveal
            [:.content
             {:visibility "visible"}]]]])

(defelement -td
  :tag :td
  :class [:py2 :bwb1 :bdb-neutral-8 :c-neutral-3 :align-middle]
  :style [])

(defn process-col-classes [classes]
  (if (string? classes)
    classes
    (str/join " " (map name classes))))

(defn last-column? [config idx]
  (= (inc idx) (count config)))

(defn middle-column? [config idx]
  (and (not (zero? idx))
       (not (last-column? config idx))))

(defn render-thead [config data]
  [:thead
   [-thead-tr
    (map-indexed
     (fn [idx c]
       (let [header-content (:header/content c)]
         [-th {:key (or (:header/key c) (:key c))
               :class (class-names
                       {:pl2 (zero? idx)
                        :pr2 (last-column? config idx)
                        :px1 (middle-column? config idx)
                        (process-col-classes (:header/class c)) true})}
          [:div.content
           header-content]]))
     config)]])

(defn render-tbody [config data]
  [:tbody
   (map-indexed
    (fn [row-idx d]
      [-tbody-tr
       {:key (or (:row/key d) (:id d))} (map-indexed
        (fn [idx c]
          (let [cell-key (or (:cell/key c) (:key c))
                cell-content (:cell/content c)]
            [-td
             {:key cell-key
              :class (class-names
                      {:pl2 (zero? idx)
                       :pr2 (last-column? config idx)
                       :px1 (middle-column? config idx)
                       (process-col-classes (:cell/class c)) true})}
             [:div.content
              (cond
                (keyword? cell-content) (get d cell-content)
                (vector? cell-content) (conj cell-content d)
                (fn? cell-content) (cell-content d)
                :else cell-content)]])) 
        config)])
    data)])

(defn render [config data]
  [-table
   [render-thead config data]
   [render-tbody config data]])
