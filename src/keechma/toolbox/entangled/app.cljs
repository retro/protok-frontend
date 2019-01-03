(ns keechma.toolbox.entangled.app
  (:require [keechma.toolbox.entangled.shared :refer [id]]
            [keechma.toolbox.entangled.controller :as controller]
            [clojure.string :as str])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn convert-namespaced-key [k]
  (if-let [k-ns (namespace k)]
    (str (str/replace k-ns "." "_") "__" (name k))
    k))

(defn component-state-sub [app-db-atom component-name component-id]
  (reaction
   (get-in @app-db-atom [:kv id component-name component-id :state])))

(defn add-component-state-subscription-dep-to-components [app-config]
  (let [cs (reduce-kv
            (fn [acc k c]
              (if (get c id)
                (let [s-deps (or (:subscription-deps c) [])]
                  (assoc-in acc [k :subscription-deps] (conj s-deps id)))
                acc))
            (:components app-config) (:components app-config))]
    (assoc app-config :components cs)))

(defn add-entangled-name-to-components [app-config]
  (let [cs (reduce-kv
            (fn [acc k c]
              (if (get c id)
                (let [ent-name (keyword :keechma.toolbox.entangled.component (convert-namespaced-key k))]
                  (assoc-in acc [k :keechma.toolbox.entangled/name] ent-name))
                acc))
            (:components app-config) (:components app-config))]
    (assoc app-config :components cs)))

(defn add-entangled-controllers [app-config]
  (reduce-kv
   (fn [acc k c]
     (if (get c id)
       (let [ent-name (get c :keechma.toolbox.entangled/name)
             actions (get c :keechma.toolbox.entangled/actions)
             controllers (:controllers acc)]
         (assoc acc :controllers (controller/register controllers ent-name actions)))
       acc))
   app-config (:components app-config)))

(defn install [app-config]
  (-> app-config
      (assoc-in [:subscriptions id] component-state-sub)
      (add-component-state-subscription-dep-to-components)
      (add-entangled-name-to-components)
      (add-entangled-controllers)))
