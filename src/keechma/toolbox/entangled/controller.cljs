(ns keechma.toolbox.entangled.controller
  (:require [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.controller :as controller]
            [cljs.core.async :refer [<! put!]]
            [keechma.toolbox.entangled.shared :refer [id ComponentCommand ->ComponentCommand swap-comp-state]]
            [promesa.core :as p]
            [medley.core :refer [dissoc-in]]
            [keechma.toolbox.entangled.pipeline :as epp])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def default-pipelines
  {:keechma.toolbox.entangled.actions/reset
   (pipeline! [value app-db]
     (epp/comp-commit! value))
   :keechma.toolbox.entangled.actions/swap
   (pipeline! [value app-db ctx]
     (epp/comp-commit! (swap-comp-state (epp/get-state app-db ctx) value)))})

(defn get-real-pipeline-name [pipelines pipeline-name]
  (let [pipeline (get pipelines pipeline-name)]
    (if (keyword? pipeline)
      (get-real-pipeline-name pipelines pipeline)
      pipeline-name)))

(defn make-run-pipeline [this app-db-atom in-chan]
  (let [component-name (:component-name this)
        pipelines      (:pipelines this)]
    (fn [command payload pipelines$]
      (let [component-id (:id payload)
            args (:args payload)
            pipeline-name      command
            real-pipeline-name (get-real-pipeline-name pipelines pipeline-name)
            pipeline           (pipelines real-pipeline-name)
            pipeline-name-id   [real-pipeline-name (keyword (gensym component-id))]
            context            {:component-name component-name
                                :component-id component-id
                                :app-db-path [:kv id component-name component-id]}
            ctrl-with-extras   (-> this
                                   (assoc :pipeline/running pipeline-name-id)
                                   (assoc-in [:context :keechma.toolbox.entangled/component]
                                             context))]
        (when pipeline
          (swap! pipelines$ assoc-in pipeline-name-id
                 {:running? true
                  :args     args 
                  :promise  (->> (pipeline ctrl-with-extras app-db-atom args pipelines$)
                                 (p/map (fn [val]
                                          (swap! pipelines$ dissoc-in pipeline-name-id)
                                          val))
                                 (p/error (fn [err]
                                            (swap! pipelines$ dissoc-in pipeline-name-id)
                                            (throw err))))}))))))

(defrecord EntangledController [component-name pipelines])

(defmethod controller/params EntangledController [_ _] true)

(defmethod controller/handler EntangledController [this app-db-atom in-chan _]
  (let [run-pipeline (make-run-pipeline this app-db-atom in-chan)
        component-name (:component-name this)]
    (go-loop [components-state  {}]
      (let [[command args] (<! in-chan)]
        (when command
          (if-not (instance? ComponentCommand args)
            (let [registered-components-ids (keys components-state)]
              (doseq [id registered-components-ids]
                (put! in-chan [command (->ComponentCommand id args)]))
              (recur components-state))
            (case command
              :on-init      (let [pipelines$      (atom {})
                                  component-state {:pipelines$ pipelines$
                                                   :current (:args args)}
                                  component-id    (:id args)]
                              (run-pipeline command args pipelines$)
                              (recur (assoc components-state component-id component-state)))

              :on-args (let [component-id (:id args)
                             args (:args args)]
                         (swap! app-db-atom assoc-in [:kv id component-name component-id :args] args)
                         (recur components-state))

              :on-terminate (let [component-id (:id args)
                                  component-state (get components-state component-id)
                                  pipelines$ (:pipelines$ component-state)]
                              (when pipelines$
                                (reset! pipelines$ {}))
                              (when component-state
                                (->> (run-pipeline command args pipelines$)
                                     (p/map (fn [_]
                                              (swap! app-db-atom dissoc-in [:kv id component-name component-id])))
                                     (p/error (fn [err]
                                                (swap! app-db-atom dissoc-in [:kv id component-name component-id])
                                                (throw err)))))
                              (recur (dissoc components-state id)))
              :on-state-change (let [component-id (:id args)
                                     component-state (get components-state component-id) 
                                     current (:args args)
                                     prev (:current component-state)
                                     pipelines$ (:pipelines$ component-state)]
                                 (if component-state
                                   (do
                                     (run-pipeline command (assoc args :args {:prev prev :current current}) pipelines$)
                                     (recur (assoc-in components-state [component-id :current] current)))
                                   (recur components-state)))
              (let [id (:id args)
                    component-state (components-state id)]
                (when component-state
                  (run-pipeline command args (:pipelines$ component-state)))
                (recur components-state)))))))))

(defn register
  ([component-name pipelines]
   (register {} component-name pipelines))
  ([controllers component-name pipelines]
   (assoc controllers component-name (->EntangledController component-name (merge pipelines default-pipelines)))))

