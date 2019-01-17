(ns protok.ui.main
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd route>]]
            [clojure.core.match :refer-macros [match]]))

(def default-page-config
  {:loading? (constantly false)})

(defn path-current-organization [ctx]
  (when-let [current-organization (sub> ctx :current-organization)]
    {:label (:name current-organization)
     :url {:page "organizations" :subpage "view" :id (:id current-organization)}}))

(defn path-current-project [ctx]
  (when-let [current-project (sub> ctx :current-project)]
    {:label (:name current-project)
     :url {:page "projects" :subpage "view" :id (:id current-project)}}))

(defn path-current-flow [ctx]
  (when-let [current-flow (sub> ctx :current-flow)]
    {:label (:name current-flow)
     :url {:page "flows" :subpage "view" :id (:id current-flow)}}))

(defn realize-page-path [ctx path]
  (let [realized 
        (map
         (fn [p]
           (cond
             (fn? p) (p ctx)
             (string? p) {:label p}
             :else p))
         path)
        full (concat [{:label "Organizations" 
                       :url {:page "organizations" :subpage "index"}}]
                     (doall realized))]
    (when-not (some nil? full)
      full)))

(defn wrap-bare-layout [ctx props]
  [(ui/component ctx :component/layout)
   {:layout/path (:path props)} 
   (:content props)])

(defn wrap-content-layout [ctx props]
  [(ui/component ctx :component/content-layout) props])

(defn render-page [ctx {:keys [component path] :as page-props}]
  (if (keyword? component)
    (let [renderer (ui/component ctx component)
          renderer-meta (meta renderer)
          renderer-context (:keechma.ui-component/context renderer-meta)
          {:keys [layout loading?]} (merge default-page-config (:protok/config renderer-context))
          props (merge
                 page-props
                 {:loading?  (partial loading? renderer-context)
                  :content [renderer]
                  :path (realize-page-path ctx (or path []))})]
      (case layout
        :bare [wrap-bare-layout ctx props]
        :content [wrap-content-layout ctx props]
        [renderer]))
    component))

(defn get-page [ctx]
  (let [route (route> ctx)]
    (match [route]
      [{:page "loading"}] :loading
      [{:page "login"}]   :login

      [{:page "organizations" :subpage "index"}]      
      {:component :organizations/list}

      [{:page "organizations" :subpage "new"}]        
      {:component :organizations/form
       :path      ["New Organization"]}

      [{:page "organizations" :subpage "edit" :id _}] 
      {:component :organizations/form
       :path      [path-current-organization "Edit Organization"]}

      [{:page "organizations" :subpage "view" :id _}] 
      {:component :projects/list
       :path      [path-current-organization "Projects"]}

      [{:page "projects" :subpage "new"}]             
      {:component :projects/form
       :path      [path-current-organization "New Project"]}

      [{:page "projects" :subpage "edit" :id _}]      
      {:component :projects/form
       :path      [path-current-organization path-current-project "Edit Project"]}

      [{:page "projects" :subpage "view" :id _}]      
      {:component :flows/list
       :path      [path-current-organization path-current-project "Flows"]}

      [{:page "flows" :subpage "new"}]                
      {:component :flows/form
       :path      [path-current-organization path-current-project "New Flow"]}
      
      [{:page "flows" :subpage "edit" :id _}]         
      {:component :flows/editor
       :path      [path-current-organization path-current-project path-current-flow]}
      
      [{:page "flows" :subpage "view" :id _}]         
       {:component :flows/editor
        :path      [path-current-organization path-current-project path-current-flow]}

      :else :not-found)))

(defn render [ctx]
  (let [page (get-page ctx)]
    [render-page ctx (if (map? page) page {:component page})]))

(def component
  (ui/constructor
   {:renderer render
    :subscription-deps [:current-organization
                        :current-project
                        :current-flow]
    :component-deps [:loading
                     :login
                     :not-found
                     :organizations/list
                     :organizations/form
                     :projects/list
                     :projects/form
                     :flows/list
                     :flows/form
                     :flows/editor
                     :component/layout
                     :component/content-layout]}))
