(ns protok.ui.editor.fsm.core)

(defrecord StateTransition [name state event-override])

(defn state-dispatcher [fsm & args]
  (get-in fsm [:current :name]))

(defmulti on-enter state-dispatcher)
(defmulti on-event state-dispatcher)
(defmulti on-exit state-dispatcher)

(defmethod on-enter :default [fsm ev state world]
  state)

(defmethod on-exit :default [fsm ev state world]
  state)

(defmethod on-event :default [fsm ev state world]
  state)

(defn create [definition]
  ;; TODO: Check if all states listed in transitions actually exist
  (let [states (set (keys definition))]
    {:states states
     :transitions (into {} (map (fn [[s t]] [s (set t)])) definition)}))

(defn transition-to
  ([next-name state] (transition-to next-name state nil))
  ([next-name state event-override]
   (->StateTransition next-name state event-override)))

(defn get-next-state [fsm event [action _] world]
  (let [current-state (get-in fsm [:current :state])
        handler (case action
                  :on-enter on-enter
                  :on-event on-event
                  :on-exit on-exit)]
    (handler fsm event current-state world)))

(defn get-next-actions [fsm next-state [action current-name] actions]
  (if (instance? StateTransition next-state)
    (let [next-name (:name next-state)
          e-override (:event-override next-state)]
      (case action
        :on-enter [[:on-enter next-name e-override] [:on-event next-name e-override]]
        :on-event [[:on-exit current-name] [:on-enter next-name e-override] [:on-event next-name e-override]]
        :on-exit  [[:on-enter next-name e-override] [:on-event next-name e-override]]))
    actions))

(defn get-next-fsm [fsm state]
  (let [prev (:current fsm)
        transition? (instance? StateTransition state)]
    (if transition?
      (let [next-state (:state state)
            next-name (:name state)
            current-name (get-in fsm [:current :name])
            valid-transitions (get-in fsm [:transitions current-name])]
        (if-not (contains? valid-transitions next-name)
          (throw (ex-info "FSM/InvalidTransition" {:current current-name :next next-name}))
          (assoc fsm :prev prev :current {:state next-state :name next-name})))
      (let [current (assoc prev :state state)]
        (assoc fsm :prev prev :current current)))))

(defn run 
  ([fsm event] (run fsm event nil))
  ([fsm event world]
   (loop [actions [[:on-event (get-in fsm [:current :name])]]
          fsm fsm]
     (if-not (seq actions)
       fsm
       (let [action (first actions)
             [_ state-name event-override] action
             fsm-with-name (assoc-in fsm [:current :name] state-name)
             rem-actions (rest actions)
             next-state (get-next-state fsm-with-name (or event-override event) action world)
             next-actions (get-next-actions fsm-with-name next-state action rem-actions)
             next-fsm (get-next-fsm fsm-with-name next-state)]
         (recur next-actions next-fsm))))))

(defn init
  ([fsm init-state] (init fsm init-state nil))
  ([fsm init-state world]
   (if-not (contains? (:states fsm) init-state)
     (throw (ex-info "FSM/NonExistingState" {:state init-state}))
     (run (assoc fsm :current {:name init-state}) nil world))))
