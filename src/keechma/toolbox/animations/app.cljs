(ns keechma.toolbox.animations.app
  (:require [keechma.toolbox.animations.shared :refer [id]]
            [keechma.toolbox.animations.core :as core])
  (:require-macros [reagent.ratom :refer [reaction]]))

(defn animation-state-sub
  ([app-db-atom animation-id] (animation-state-sub app-db-atom animation-id nil))
  ([app-db-atom animation-id animation-version]
   (reaction
    (core/get-animation @app-db-atom animation-id animation-version))))

(defn add-animation-state-subscription-dep-to-components [app-config]
  (assoc app-config :components
         (reduce-kv (fn [acc k c]
                      (let [s-deps (or (:subscription-deps c) [])]
                        (assoc-in acc [k :subscription-deps] (conj s-deps id))))
                    (:components app-config) (:components app-config))))

(defn install [app-config]
  (-> app-config
      (assoc-in [:subscriptions id] animation-state-sub)
      (add-animation-state-subscription-dep-to-components)))
