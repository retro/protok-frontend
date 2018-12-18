(ns protok.ui.editor.states.aligning
  (:require [protok.ui.editor.fsm.core :as fsm]
            [protok.ui.editor.states.shared :refer
             [get-selected-layers
              rect->coordinates
              get-leftmost-layer
              get-topmost-layer]]))

(defn align-min [attr state]
   (let [layers (get-selected-layers state)
         val (apply min (map #(get-in % [:rect attr]) layers))]
     (reduce (fn [acc l]
               (assoc-in acc [:document :layers (:id l) :rect attr] val))
             state layers)))

(defn align-vertical-center [state]
  (let [layers (get-selected-layers state)
        coordinates (map #(rect->coordinates (:rect %)) layers)
        min-y (apply min (map :y1 coordinates))
        max-y (apply max (map :y2 coordinates))
        center-y (+ min-y (/ (- max-y min-y) 2))]
    (reduce (fn [acc l]
              (let [{:keys [top height]} (:rect l)
                    l-center-y (+ top (/ height 2))]
                (assoc-in acc [:document :layers (:id l) :rect :top] (+ top (- center-y l-center-y)))))
            state layers)))

(defn align-horizontal-center [state]
  (let [layers (get-selected-layers state)
        coordinates (map #(rect->coordinates (:rect %)) layers)
        min-x (apply min (map :x1 coordinates))
        max-x (apply max (map :x2 coordinates))
        center-x (+ min-x (/ (- max-x min-x) 2))]
    (reduce (fn [acc l]
              (let [{:keys [left width]} (:rect l)
                    l-center-x (+ left (/ width 2))]
                (assoc-in acc [:document :layers (:id l) :rect :left] (+ left (- center-x l-center-x)))))
            state layers)))

(defn align-horizontal-right [state]
  (let [layers (get-selected-layers state)
        coordinates (map #(rect->coordinates (:rect %)) layers)
        max-x (apply max (map :x2 coordinates))]
    (reduce (fn [acc l]
              (let [{:keys [left width]} (:rect l)
                    l-x2 (+ left width)]
                (assoc-in acc [:document :layers (:id l) :rect :left] (+ left (- max-x l-x2)))))
            state layers)))

(defn align-vertical-bottom [state]
  (let [layers (get-selected-layers state)
        coordinates (map #(rect->coordinates (:rect %)) layers)
        max-y (apply max (map :y2 coordinates))]
    (reduce (fn [acc l]
              (let [{:keys [top height]} (:rect l)
                    l-y2 (+ top height)]
                (assoc-in acc [:document :layers (:id l) :rect :top] (+ top (- max-y l-y2)))))
            state layers)))

(defn align-horizontal-spacing [state]
  (let [layers (sort-by #(get-in % [:rect :left]) (get-selected-layers state))
        layers-to-move (vec (drop 1 layers))
        coordinates (mapv #(rect->coordinates (:rect %)) layers)
        width-sum (apply + (map #(get-in % [:rect :width]) layers))
        min-x (apply min (map :x1 coordinates))
        max-x (apply max (map :x2 coordinates))
        spacing (/ (- max-x min-x width-sum) (count layers-to-move))]
    (-> (reduce
         (fn [{:keys [last-x2 state]} l]
           (let [new-left (+ last-x2 spacing)]
             {:state (assoc-in state [:document :layers (:id l) :rect :left] new-left)
              :last-x2 (+ new-left (get-in l [:rect :width]))}))
         {:last-x2 (get-in coordinates [0 :x2])
          :state state}
         layers-to-move)
        :state)))

(defn align-vertical-spacing [state]
  (let [layers (sort-by #(get-in % [:rect :top]) (get-selected-layers state))
        layers-to-move (vec (drop 1 layers))
        coordinates (mapv #(rect->coordinates (:rect %)) layers)
        height-sum (apply + (map #(get-in % [:rect :height]) layers))
        min-y (apply min (map :y1 coordinates))
        max-y (apply max (map :y2 coordinates))
        spacing (/ (- max-y min-y height-sum) (count layers-to-move))]
    (-> (reduce
         (fn [{:keys [last-y2 state]} l]
           (let [new-top (+ last-y2 spacing)]
             {:state (assoc-in state [:document :layers (:id l) :rect :top] new-top)
              :last-y2 (+ new-top (get-in l [:rect :height]))}))
         {:last-y2 (get-in coordinates [0 :y2])
          :state state}
         layers-to-move)
        :state)))

(defn align-selected [ev state]
  (let [alignment (:payload ev)]
    (case alignment
      :vertical-top       (align-min :top state)
      :vertical-center    (align-vertical-center state)
      :vertical-bottom    (align-vertical-bottom state)
      :vertical-spacing   (align-vertical-spacing state)
      :horizontal-left    (align-min :left state)
      :horizontal-center  (align-horizontal-center state)
      :horizontal-right   (align-horizontal-right state)
      :horizontal-spacing (align-horizontal-spacing state)
      state)))

(defmethod fsm/on-enter :editor/aligning [_ ev state _]
  (fsm/transition-to :editor/selected (align-selected ev state) {}))
