(ns protok.ui.editor.states.selected
  (:require [protok.ui.editor.fsm.core :as fsm]
            [protok.ui.editor.states.shared :refer [mouse-moved-while-down? mouse-on-selected? get-layer-under-mouse add-selected-bounding-box]]
            [medley.core :refer [dissoc-in]]))

(defn on-mouse-down-on-not-selected [ev state]
  (let [p (:payload ev)
        layer-under-mouse (get-layer-under-mouse p :mouse-down state)]
    (if layer-under-mouse
      (fsm/transition-to :editor/dragging (assoc-in state [:interaction :selected] #{(:id layer-under-mouse)}))
      (fsm/transition-to :editor/selecting state))))

(defn on-mouse-up [ev state]
  (let [p (:payload ev)
        layer-under-mouse (get-layer-under-mouse p :mouse-up state)
        layer-under-mouse-id (:id layer-under-mouse)
        selected (get-in state [:interaction :selected])]
    (if (contains? selected layer-under-mouse-id)
      state
      (-> state
          (assoc-in [:interaction :selected] #{layer-under-mouse-id})
          (add-selected-bounding-box)))))

(defmethod fsm/on-enter :editor/selected [_ _ state _]
  (add-selected-bounding-box state))

(defmethod fsm/on-event :editor/selected [_ ev state _]
  (let [p (:payload ev)
        ev-type (:type ev)]
 
    (cond
      (= :mouse-up ev-type) (on-mouse-up ev state)

      (and (not (mouse-on-selected? p :mouse-down state))
           (= :mouse-down ev-type))
      (on-mouse-down-on-not-selected ev state) 

      (and (mouse-on-selected? p :mouse-down state)
           (mouse-moved-while-down? p))
      (fsm/transition-to :editor/dragging state) 
      
      (= :align ev-type) (fsm/transition-to :editor/aligning state)
      
      :else state)))
