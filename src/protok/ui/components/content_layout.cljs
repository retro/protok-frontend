(ns protok.ui.components.content-layout
  (:require [keechma.ui-component :as ui]
            [protok.ui.components.elements :as e]
            [protok.ui.shared :refer [datasources-pending?>]]
            [protok.icons :refer [icon]]))

(defn render-path-part [ctx {:keys [label url]}]
  (if url
    [:a {:href (ui/url ctx url)} label]
    [:span label]))

(defn loading? [ctx page-loading?]
  (or (page-loading?) 
      (datasources-pending?>
       ctx 
       :current-organization
       :current-project
       :current-flow)))

(defn render [ctx {:keys [content path] :as props}]
  (let [breadcrumbs (butlast path)
        title (last path)
        last-breadcrumb? (fn [idx] (= idx (dec (count breadcrumbs))))]
    [(ui/component ctx :component/layout)
     [e/-layout 
      (if (loading? ctx (:loading? props))
        [:div]
        [:div
         [e/-breadcrumbs-wrap
          (map-indexed 
           (fn [idx p]
             ^{:key idx}
             [:li
              [render-path-part ctx p]
              (when-not (last-breadcrumb? idx)
                [:div.chevron.inline-block
                 (icon :chevron-right)])])
           breadcrumbs)]
         [e/-page-title [render-path-part ctx title]]
         [e/-content-wrap
          content]])]]))

(def component
  (ui/constructor {:renderer render
                   :component-deps [:component/layout]}))
