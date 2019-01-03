(ns protok.controllers.node-form-mounter
  (:require [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]
            [keechma.toolbox.forms.core :refer [id-key]]
            [protok.edb :as edb]
            [protok.domain.db :as db]))

(def controller 
  (pp-controller/constructor
   (fn [route]
     (let [{:keys [page subpage id node-id]} (:data route)]
       (when (and (= "flows" page) (= "edit" subpage) id)
         node-id)))
   {:on-start (pipeline! [value app-db]
                (wait-dataloader-pipeline!)
                (pp/send-command! [id-key :mount-form] [(db/get-current-flow-node-form-type app-db) value]))
    :on-stop (pipeline! [value app-db]
               (pp/send-command! [id-key :unmount-form] [(db/get-current-flow-node-form-type app-db) value]))}))

(defn register
  ([] (register {}))
  ([controllers] (assoc controllers ::id controller)))
