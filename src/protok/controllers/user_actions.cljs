(ns protok.controllers.user-actions
  (:require [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]
            [protok.domain.db :as db]
            [protok.util.local-storage :refer [ls-remove!]]
            [protok.settings :refer [jwt-ls-name]]))

(def controller
  (pp-controller/constructor
   (constantly true)
   {:logout (pipeline! [value app-db]
              (ls-remove! jwt-ls-name)
              (pp/commit!
               (-> app-db
                   (db/dissoc-jwt)
                   (db/remove-current-account)))
              (pp/reroute!))}))

(defn register
  ([] (register {}))
  ([controllers] (assoc controllers :user-actions controller)))
