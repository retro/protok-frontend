(ns protok.ui.editor.states.selecting
  (:require [protok.ui.editor.fsm.core :as fsm]
            [protok.ui.editor.states.shared :refer 
             [rect->coordinates
              mouse-moved-while-down?
              mouse-on-selected?
              get-layer-under-mouse
              layers-under-selection?]]
            [medley.core :refer [dissoc-in]]))

(defn add-selection [state ev]
  (let [payload (:payload ev)
        mouse-down (:mouse-down payload)
        mouse (:mouse payload)
        [x1 x2] (sort [(:x mouse-down) (:x mouse)])
        [y1 y2] (sort [(:y mouse-down) (:y mouse)])]
    (assoc-in state [:interaction :selection]
              {:left x1
               :top y1
               :width (- x2 x1)
               :height (- y2 y1)})))

(defn calculate-selected [layers {:keys [x1 x2 y1 y2]}] 
  (->> (filter 
        (fn [s]
          (let [{s-x1 :x1 s-x2 :x2 s-y1 :y1 s-y2 :y2} (rect->coordinates (:rect s))]
            (and (<= x1 s-x1 s-x2 x2)
                 (<= y1 s-y1 s-y2 y2))))
        layers)
       (map :id)
       set))

(defn add-selected [state ev]
  (let [payload    (:payload ev)
        document   (:document state)
        mouse-down (:mouse-down payload)
        mouse      (:mouse payload)
        [x1 x2]    (sort [(:x mouse-down) (:x mouse)])
        [y1 y2]    (sort [(:y mouse-down) (:y mouse)])]
     (assoc-in state [:interaction :selected]
               (calculate-selected (vals (:layers document)) {:x1 x1 :x2 x2 :y1 y1 :y2 y2}))))

(defn on-mouse-up [ev state]
  (let [p (:payload ev)
        layer-under-mouse (get-layer-under-mouse p :mouse-up state)
        selected (get-in state [:interaction :selected])
        selection (get-in state [:interaction :selection])
        state-without-selection (dissoc-in state [:interaction :selection])]
    
    (cond
      (and (not (layers-under-selection? selection))
           (not (mouse-on-selected? p :mouse-up state))
           layer-under-mouse)
      (fsm/transition-to :editor/selected (assoc-in state-without-selection [:interaction :selected] #{(:id layer-under-mouse)}) {})

      (seq selected)
      (fsm/transition-to :editor/selected state-without-selection {})

      :else (fsm/transition-to :editor/init state-without-selection {}))))

(defn on-mouse-leave [ev state]
  (let [selected (get-in state [:interaction :selected])
        next-state-name (if (seq selected) :editor/selected :editor/init)]
    (fsm/transition-to next-state-name (dissoc-in state [:interaction :selection]))))

(defmethod fsm/on-event :editor/selecting [_ ev state _]
  (cond
    (= :mouse-up (:type ev)) (on-mouse-up ev state)
    (= :mouse-leave (:type ev)) (on-mouse-leave ev state)
    :else (-> state
              (add-selection ev)
              (add-selected ev))))

(defmethod fsm/on-enter :editor/selecting [_ _ state _]
  (dissoc-in state [:interaction :selected-bounding-box]))
