(ns protok.ui.editor.states.dragging
  (:require [protok.ui.editor.fsm.core :as fsm]
            [medley.core :refer [dissoc-in]]
            [protok.ui.editor.states.shared :refer [add-selected-bounding-box]]))

(defn get-translate [payload]
  (let [mouse-down (:mouse-down payload)
        mouse (:mouse payload)]
    {:x (- (:x mouse) (:x mouse-down))
     :y (- (:y mouse) (:y mouse-down))}))

(defn materialize-translate [state]
  (let [{:keys [x y]} (get-in state [:interaction :translate-selected])
        selected (get-in state [:interaction :selected])]
    (println "SELECTED" selected)
    (reduce
     (fn [acc id] 
       (-> acc
           (update-in [:document :layers id :rect :top] + y)
           (update-in [:document :layers id :rect :left] + x))) 
     state
     selected)))

(defmethod fsm/on-enter :editor/dragging [_ _ state _]
  (add-selected-bounding-box state))

(defmethod fsm/on-event :editor/dragging [_ ev state _]
  (cond
    (= :mouse-up (:type ev)) (fsm/transition-to :editor/selected state {})
    :else (assoc-in state [:interaction :translate-selected] (get-translate (:payload ev)))))

(defmethod fsm/on-exit :editor/dragging [_ _ state _]
  (-> state
      materialize-translate
      (dissoc-in [:interaction :translate-selected])))
