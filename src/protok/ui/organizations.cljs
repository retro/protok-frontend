(ns protok.ui.organizations
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.elements :as e]
            [protok.ui.shared :refer [datasources-pending?>]]
            [keechma.toolbox.ui :refer [route>]]
            [clojure.core.match :refer-macros [match]]))

(defelement -wrap)

(defn render [ctx]
  (let [route (route> ctx)]
    [(ui/component ctx :component/layout)
     [e/-layout 
      [e/-page-title "Organizations"]
      [e/-content-wrap
       (if (datasources-pending?> ctx :organizations)
         "Loading"
         (match [route]
           [{:subpage "index"}] [(ui/component ctx :organizations/list)]
           [{:subpage "new"}] [(ui/component ctx :organizations/form)]
           [{:subpage "edit" :id _}] [(ui/component ctx :organizations/form)]
           :else nil))]]]))

(def component
  (ui/constructor {:renderer render
                   :component-deps [:component/layout
                                    :organizations/list
                                    :organizations/form]}))
