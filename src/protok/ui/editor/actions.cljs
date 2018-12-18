(ns protok.ui.editor.actions
  (:require [keechma.toolbox.entangled.pipeline :as epp]
            [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.tasks :as t]
            [oops.core :refer [oget ocall oget+]]
            [protok.ui.editor.interaction :refer [make-events-producer]]
            [protok.ui.editor.mock-state :as mock-state]
            [protok.ui.editor.fsm.core :as fsm]
            [protok.ui.editor.states.init]
            [protok.ui.editor.states.selected]
            [protok.ui.editor.states.selecting]
            [protok.ui.editor.states.dragging]
            [protok.ui.editor.states.aligning])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def interaction-fsm
  (fsm/create
   {:editor/init [:editor/selecting :editor/selected]
    :editor/selecting [:editor/selected :editor/init]
    :editor/selected [:editor/selecting :editor/dragging :editor/aligning]
    :editor/dragging [:editor/selected]
    :editor/aligning [:editor/selected]}))

(defn start-events-manager! [ctx el]
  (let [state-path (conj (epp/get-app-db-path ctx) :state)]
    (t/blocking-task!
     (make-events-producer el)
     [::events-manager (epp/get-id ctx)]
     (fn [{:keys [id value]} app-db]
       (let [current-state (get-in app-db state-path)]
         (assoc-in app-db state-path (fsm/run current-state value)))))))

(defn fsm-run! [app-db ctx ev]
  (let [current-state (epp/get-state app-db ctx)]
    (epp/comp-commit! (fsm/run current-state ev))))

(def actions
  {:on-init (pipeline! [value app-db ctx]
              (epp/comp-commit! (fsm/init interaction-fsm :editor/init {:document mock-state/state})))
   :register-element (pipeline! [value app-db ctx]
                       (start-events-manager! ctx value))
   :align (pipeline! [value app-db ctx]
            (fsm-run! app-db ctx {:type :align :payload value}))})
