(ns protok.controllers.flow-editor
  (:require [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.toolbox.dataloader.controller :refer [run-dataloader!]]
            [protok.gql :as gql]
            [protok.domain.db :as db]
            [keechma.toolbox.forms.core :as forms-core]))

(def controller 
  (pp-controller/constructor
   (fn [route]
     (let [{:keys [page subpage id node-id]} (:data route)]
       (when (and (= "flows" page) (= "edit" subpage) id)
         node-id)))
   {:delete-node (pipeline! [value app-db]
                   (when (js/confirm "Are you sure?")
                     (pipeline! [value app-db]
                       (gql/m! :delete-flow-node {:id value} (db/get-jwt app-db))
                       (run-dataloader! [:current-flow])
                       (pp/redirect! (dissoc (get-in app-db [:route :data]) :node-id)))))}))

(defn register
  ([] (register {}))
  ([controllers] (assoc controllers :flow-editor controller)))
