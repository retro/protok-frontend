(ns protok.ui.editor.states.init
  (:require [protok.ui.editor.fsm.core :as fsm]
            [protok.ui.editor.states.shared :refer [mouse-moved-while-down? get-layer-under-mouse]]
            [medley.core :refer [dissoc-in]]))

(defn on-mouse-down [ev state]
  (let [p (:payload ev)
        layer-under-mouse (get-layer-under-mouse p :mouse-down state)]
    (if layer-under-mouse
      (fsm/transition-to :editor/selected (assoc-in state [:interaction :selected] #{(:id layer-under-mouse)}))
      state)))

(defmethod fsm/on-event :editor/init [_ ev state world]
  (cond
    (nil? ev) world
    (mouse-moved-while-down? (:payload ev)) (fsm/transition-to :editor/selecting state)
    (= :mouse-down (:type ev)) (on-mouse-down ev state)
    :else state))

(defmethod fsm/on-enter :editor/init [_ _ state _]
  (dissoc-in state [:interaction :selected-bounding-box]))
