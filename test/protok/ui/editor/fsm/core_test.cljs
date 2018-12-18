(ns protok.ui.editor.fsm.core-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [protok.ui.editor.fsm.core :as fsm]))

(def fsm
  (fsm/create {:editor/init            [:editor/selecting :editor/dragging-canvas]
               :editor/selecting       #{:editor/init :editor/selected}
               :editor/selected        [:editor/selecting :editor/dragging :editor/init]
               :editor/dragging        [:editor/selected]
               :editor/dragging-canvas #{:editor/init}}))

(defn mark-state [state name handler-name]
  (let [max-value (or (apply max (flatten (map vals (vals state)))) 0)
        prev-calls (or (get-in state [name handler-name]) [])]
    (assoc-in state [name handler-name] (conj prev-calls (inc max-value)))))

(defmethod fsm/on-enter :editor/init [_ ev state _]
  (mark-state state :init :enter))

(defmethod fsm/on-event :editor/init [_ ev state _]
  (cond
    (= :mousedown ev) (fsm/transition-to :editor/selecting (mark-state state :init :event))
    :else (mark-state state :init :event)))

(defmethod fsm/on-exit :editor/init [_ _ state _]
  (mark-state state :init :exit))

(defmethod fsm/on-enter :editor/selecting [_ _ state _]
  (mark-state state :selecting :enter))

(defmethod fsm/on-event :editor/selecting [_ ev state _]
  (case ev
    :mousedown (mark-state state :selecting :event)
    :mouseup (fsm/transition-to :editor/selected (mark-state state :selecting :event))
    :keyup (fsm/transition-to :editor/init (mark-state state :selecting :event))))

(defmethod fsm/on-exit :editor/selecting [_ ev state _]
  (mark-state state :selecting :exit))

(defmethod fsm/on-enter :editor/selected [_ ev state _]
  (case ev
    :mouseup (fsm/transition-to :editor/init (mark-state state :selected :enter))))

(defmethod fsm/on-event :editor/selected [_ ev state _]
  (mark-state state :editor :selected))

(deftest create
  (is (= {:states      #{:editor/init :editor/selecting :editor/selected :editor/dragging-canvas :editor/dragging}
          :transitions {:editor/init            #{:editor/selecting :editor/dragging-canvas}
                        :editor/selecting       #{:editor/init :editor/selected}
                        :editor/selected        #{:editor/selecting :editor/dragging :editor/init}
                        :editor/dragging        #{:editor/selected}
                        :editor/dragging-canvas #{:editor/init}}}
         
      fsm)))

(deftest init
  (is (= (select-keys (fsm/init fsm :editor/init) [:prev :current])
         {:prev {:name :editor/init} :current {:name :editor/init :state {:init {:event [1]}}}})))

(deftest state-transition
  (is (= {:current {:name :editor/init
                    :state {:init {:enter [9 17]
                                   :event [1 2 10 11 18]
                                   :exit [3 12]}
                            :selecting {:enter [4 13]
                                        :event [5 6 14 15]
                                        :exit [7 16]}
                            :selected {:enter [8]}}}}
         (-> fsm
             (fsm/init :editor/init)
             (fsm/run :mousedown)
             (fsm/run :mouseup)
             (fsm/run :mousedown)
             (fsm/run :keyup)
             (select-keys [:current])))))

(def simple-fsm
  (fsm/create {:simple/init [:simple/state-1]
               :simple/state-1 [:simple/state-2]
               :simple/state-2 [:simple/state-3]
               :simple/state-3 [:simple/state-4 :simple/state-5]
               :simple/state-4 []
               :simple/state-5 []}))

(defmethod fsm/on-event :simple/init [_ _ state _]
  (fsm/transition-to :simple/state-1 (mark-state state :init :event)))

(defmethod fsm/on-exit :simple/init [_ _ state _]
  (mark-state state :init :exit))

(defmethod fsm/on-enter :simple/state-1 [_ _ state _]
  (fsm/transition-to :simple/state-2 (mark-state state :state-1 :enter)))

(defmethod fsm/on-event :simple/state-1 [_ _ state _]
  (mark-state state :state-1 :event))

(defmethod fsm/on-exit :simple/state-1 [_ _ state _]
  (mark-state state :state-1 :exit))

(defmethod fsm/on-enter :simple/state-2 [_ _ state _]
  (mark-state state :state-2 :enter))

(defmethod fsm/on-event :simple/state-2 [_ _ state _]
  (fsm/transition-to :simple/state-3 (mark-state state :state-2 :event)))

(defmethod fsm/on-exit :simple/state-2 [_ _ state _]
  (mark-state state :state-2 :exit))

(defmethod fsm/on-enter :simple/state-3 [_ _ state _]
  (mark-state state :state-3 :enter))

(defmethod fsm/on-event :simple/state-3 [_ _ state _]
  (fsm/transition-to :simple/state-4 (mark-state state :state-3 :event)))

(defmethod fsm/on-exit :simple/state-3 [_ _ state _]
  (fsm/transition-to :simple/state-5 (mark-state state :state-3 :exit)))

(defmethod fsm/on-enter :simple/state-4 [_ _ state _]
  (mark-state state :state-4 :enter))

(defmethod fsm/on-event :simple/state-4 [_ _ state _]
  (mark-state state :state-4 :event))

(defmethod fsm/on-exit :simple/state-4 [_ _ state _]
  (mark-state state :state-4 :exit))

(defmethod fsm/on-enter :simple/state-5 [_ _ state _]
  (mark-state state :state-5 :enter))

(defmethod fsm/on-event :simple/state-5 [_ _ state _]
  (mark-state state :state-5 :event))

(defmethod fsm/on-exit :simple/state-5 [_ _ state _]
  (mark-state state :state-5 :exit))

(deftest auto-transition
  (is (= {:current {:name :simple/state-5
                    :state {:init {:event [1]
                                   :exit [2]}
                            :state-1 {:enter [3]}
                            :state-2 {:enter [4]
                                      :event [5]
                                      :exit [6]}
                            :state-3 {:enter [7]
                                      :event [8]
                                      :exit [9]}
                            :state-5 {:enter [10]
                                      :event [11]}}}}
         (-> simple-fsm
             (fsm/init :simple/init)
             (select-keys [:current])))))

(def minimal-fsm
  {:minimal/init []})

(defmethod fsm/on-enter :minimal/init [_ _ _ _]
  (fsm/transition-to :minimal/non-existing {}))

(deftest incorrect-transition-will-throw
  (try
    (-> minimal-fsm
        (fsm/init :minimal/init))
    (is false)
    (catch :default e
      (is true))))

(deftest incorrect-init-state-will-throw
  (try
    (-> minimal-fsm
        (fsm/init :minimal/non-existing))
    (is false)
    (catch :default e
      (is true))))
