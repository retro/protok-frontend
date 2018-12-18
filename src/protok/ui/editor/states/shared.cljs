(ns protok.ui.editor.states.shared
  (:require [oops.core :refer [ocall oget]]
            [medley.core :refer [dissoc-in]]))

(def mouse-move-threshold 3)

(defn in-threshold? [threshold n1 n2]
  (> threshold (ocall js/Math :abs (- n1 n2))))

(defn mouse-moved-while-down? [payload]
  (let [mouse (:mouse payload)
        mouse-down (:mouse-down payload)]
    (if (and mouse mouse-down)
      (not (and (in-threshold? mouse-move-threshold (:x mouse) (:x mouse-down))
                (in-threshold? mouse-move-threshold (:y mouse) (:y mouse-down))))
      false)))

(defn rect->coordinates [rect]
  (let [x1 (:left rect)
        x2 (+ x1 (:width rect))
        y1 (:top rect)
        y2 (+ y1 (:height rect))]
    {:x1 x1 :x2 x2 :y1 y1 :y2 y2}))

(defn coordinates->rect [{:keys [x1 x2 y1 y2]}]
  {:top y1
   :height (- y2 y1)
   :left x1
   :width (- x2 x1)})

(defn get-layer-by-id [state id]
  (get-in state [:document :layers id]))

(defn under-coordinates? [x y l]
  (let [{l-x1 :x1 l-x2 :x2 l-y1 :y1 l-y2 :y2} (rect->coordinates (:rect l))]
    (and (<= l-x1 x l-x2)
         (<= l-y1 y l-y2))))

(defn mouse-on-selected? [payload attr state]
  (let [mouse (get payload attr)
        selected (get-in state [:interaction :selected])
        selected-bounding-box (get-in state [:interaction :selected-bounding-box])]
    (if (and (seq selected) mouse)
      (let [{:keys [x y]} mouse]
        (if selected-bounding-box
          (under-coordinates? x y {:rect selected-bounding-box})
          (-> (some #(under-coordinates? x y %) (map #(get-layer-by-id state %) selected))
              boolean)))
      false)))

(defn get-layer-under-mouse [payload attr state]
  (let [mouse (get payload attr)
        {:keys [x y]} mouse]
    (first (filter #(under-coordinates? x y %) (sort-by :order (vals (get-in state [:document :layers])))))))

(defn get-selected-layers [state]
  (let [selected (get-in state [:interaction :selected])]
    (sort-by :order (map #(get-layer-by-id state %) selected))))

(defn layers-under-selection? [selection]
  (if selection
    (let [{:keys [x1 x2 y1 y2]} (rect->coordinates selection)]
      (and (not (in-threshold? mouse-move-threshold x1 x2))
           (not (in-threshold? mouse-move-threshold y1 y2))))
    false))

(defn selected-multiple? [state]
  (let [selected (get-in state [:interaction :selected])]
    (and (seq selected) (< 1 (count selected)))))

(defn add-selected-bounding-box [state]
  (if (selected-multiple? state)
    (let [selected (get-in state [:interaction :selected])]
      (assoc-in
       state
       [:interaction :selected-bounding-box]
       (-> (reduce
            (fn [{:keys [x1 x2 y1 y2]} l]
              (let [{l-x1 :x1 l-x2 :x2 l-y1 :y1 l-y2 :y2} (rect->coordinates (:rect l))] 
                {:x1 (min x1 l-x1)
                 :x2 (max x2 l-x2)
                 :y1 (min y1 l-y1)
                 :y2 (max y2 l-y2)}))
            {:x1 js/Infinity :x2 0 :y1 js/Infinity :y2 0}
            (map #(get-layer-by-id state %) selected))
           coordinates->rect)))
    (dissoc-in state [:interaction :selected-bounding-box])))

(defn get-leftmost-layer [layers]
  (-> (reduce
       (fn [acc l]
         (let [l-left (get-in l [:rect :left])]
           (if (< l-left (:left acc))
             {:left l-left :layer l}
             acc))) 
       {:layer nil :left js/Infinity}
       layers)
      :layer))

(defn get-topmost-layer [layers]
  (-> (reduce
       (fn [acc l]
         (let [l-top (get-in l [:rect :top])]
           (if (< l-top (:top acc))
             {:top l-top :layer l}
             acc))) 
       {:layer nil :top js/Infinity}
       layers)
      :layer))
