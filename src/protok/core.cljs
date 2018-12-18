(ns ^:figwheel-hooks protok.core 
  (:require [reagent.core :as reagent]
            [keechma.app-state :as app-state]
            [keechma.toolbox.dataloader.app :as dataloader]
            [keechma.toolbox.forms.app :as forms]
            [protok.controllers :refer [controllers]]
            [protok.ui :refer [ui]]
            [protok.subscriptions :refer [subscriptions]]
            [protok.edb :refer [edb-schema]]
            [protok.datasources :refer [datasources]]
            [protok.forms :as sf-forms]
            [protok.styles :refer [stylesheet]]
            [keechma.toolbox.entangled.app :as entangled]
            [keechma.toolbox.css.app :as css]))

(def app-definition
  (-> {:components    ui
       :controllers   controllers
       :subscriptions subscriptions
       :html-element  (.getElementById js/document "app")}
      (dataloader/install datasources edb-schema)
      (forms/install sf-forms/forms sf-forms/forms-automount-fns)
      (css/install (stylesheet))
      (entangled/install)))

(defonce running-app (clojure.core/atom nil))

(defn start-app! []
  (reset! running-app (app-state/start! app-definition)))

(defn dev-setup []
  (when ^boolean js/goog.DEBUG
    (enable-console-print!)
    (println "dev mode")))

(defn ^:after-load reload []
  (let [current @running-app]
    (if current
      (app-state/stop! current start-app!)
      (start-app!))))

(defn ^:export main []
  (dev-setup)
  (start-app!))
