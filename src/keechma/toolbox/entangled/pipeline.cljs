(ns keechma.toolbox.entangled.pipeline
  (:require [keechma.toolbox.pipeline.core :as pp :refer [ISideffect]]
            [keechma.toolbox.entangled.shared :as shared :refer [id ->ComponentCommand swap-comp-state]]
            [keechma.controller :as controller]))

(def get-app-db-path shared/get-app-db-path)
(def get-id shared/get-id)
(def get-name shared/get-name)
(def get-state-app-db-path shared/get-state-app-db-path)

(defn get-state [app-db ctx]
  (let [state-app-db-path (get-state-app-db-path ctx)]
    (get-in app-db state-app-db-path)))

(defn get-args [app-db ctx]
  (let [app-db-path (get-app-db-path ctx)]
    (get-in app-db (conj app-db-path :args))))

(defn state-changed? [{:keys [prev current]} & test-keys]
  (if (empty? test-keys)
    (not= prev current)
    (let [prev (select-keys prev test-keys)
          current (select-keys current test-keys)]
      (not= prev current))))

(defrecord ComponentCommitSideffect [data]
  ISideffect
  (call! [_ controller app-db-atom _]
    (let [app-db-path (get-app-db-path (:context controller))]
      (swap! app-db-atom assoc-in (conj app-db-path :state) data))))

(defrecord ComponentExecuteSideffect [cmd args]
  ISideffect
  (call! [_ controller _ _]
    (let [component-id (get-id (:context controller))]
      (controller/execute controller cmd (->ComponentCommand component-id args)))))

(defrecord ComponentSwapSideffect [args]
  ISideffect
  (call! [_ controller app-db-atom _]
    (let [state-app-db-path (get-state-app-db-path (:context controller))
          state (get-in @app-db-atom state-app-db-path)]
      (swap! app-db-atom assoc-in state-app-db-path (swap-comp-state state args)))))

(defn comp-commit! [data]
  (->ComponentCommitSideffect data))

(defn comp-execute!
  ([cmd] (comp-execute! cmd nil))
  ([cmd args]
   (->ComponentExecuteSideffect cmd args)))

(defn comp-swap! [& args]
  (->ComponentSwapSideffect args))
