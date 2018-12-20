(ns protok.ui.main
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd route>]]
            [protok.svgs :refer [logo-picto logo-mono logo]]))

(defn render [ctx]
  (let [page (:page (route> ctx))]
    [:div
     (case page
       "loading" [(ui/component ctx :loading)]
       "login" [(ui/component ctx :login)]
       "organizations" [(ui/component ctx :organizations)]
       
       [:div "404"])]))

(def component
  (ui/constructor
   {:renderer render
    :component-deps [:loading
                     :login
                     :organizations]}))
