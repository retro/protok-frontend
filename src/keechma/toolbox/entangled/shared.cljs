(ns keechma.toolbox.entangled.shared)

(def id :keechma.toolbox.entangled/id)

(defrecord ComponentCommand [id args])

(defn get-app-db-path [ctx]
  (get-in ctx [:keechma.toolbox.entangled/component :app-db-path]))

(defn get-id [ctx]
  (get-in ctx [:keechma.toolbox.entangled/component :component-id]))

(defn get-name [ctx]
  (get-in ctx [:keechma.toolbox.entangled/component :component-name]))

(defn get-state-app-db-path [ctx]
  (vec (conj (get-app-db-path ctx) :state)))

(defn swap-comp-state [state args]
  (let [f (first args)
        args (concat [state] (rest args))]
    (apply f args)))
