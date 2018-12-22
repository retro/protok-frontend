(ns protok.ui.organizations
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.elements :as e]
            [protok.ui.shared :refer [datasources-pending?>]]
            [keechma.toolbox.ui :refer [route>]]))

(defelement -wrap)

(defn render [ctx]
  (let [{:keys [subpage]} (route> ctx)]
    [(ui/component ctx :component/layout)
     [e/-layout 
      [e/-page-title "Organizations"]
      [e/-content-wrap
       (if (datasources-pending?> ctx :organizations)
         "Loading"
         (case subpage
           "index" [(ui/component ctx :organizations/list)]
           nil))]]]))

(def component
  (ui/constructor {:renderer render
                   :component-deps [:component/layout
                                    :organizations/list]}))
