(ns protok.datasources
  (:require [protok.settings :refer [jwt-ls-name]]
            [protok.util.local-storage :refer [ls-get]]
            [keechma.toolbox.dataloader.subscriptions :refer [map-loader]]
            [protok.gql :as gql]
            [protok.domain.gql :refer [organization-membership->organization organization-processor]]))

(def ignore-datasource!
  :keechma.toolbox.dataloader.core/ignore)

(def jwt
  {:target [:kv :jwt]
   :loader (map-loader #(ls-get jwt-ls-name))
   :params (fn [prev _ _]
             (when (:data prev)
               ignore-datasource!))})

(def current-account
  {:target [:edb/named-item :account/current]
   :deps [:jwt]
   :loader gql/loader
   :params (fn [_ _ {:keys [jwt]}]
             (when jwt
               {:query [:current-account :currentAccount]
                :token jwt}))})

(def organizations
  {:target [:edb/collection :organization/list]
   :deps [:jwt]
   :loader gql/loader
   :processor #(mapv organization-membership->organization %) 
   :params (fn [_ {:keys [page subpage]} {:keys [jwt]}]
             (when (and (= "organizations" page)
                        (= "index" subpage)
                        jwt)
               {:query [:organization-memberships
                        [:currentAccount :organizationMemberships]]
                :token jwt}))})

(def current-organization
  {:target [:edb/named-item :organization/current]
   :deps [:jwt :current-project]
   :loader gql/loader
   :processor organization-processor
   :params (fn [_ {:keys [page id organization-id]} {:keys [jwt current-project]}]
             (let [organization-id
                   (cond 
                     (and (= "organizations" page) id) id
                     current-project (get-in current-project [:organization :id])
                     :else organization-id)]
               (when (and jwt organization-id)
                 {:query [:fetch-organization :fetchOrganization]
                  :variables {:id organization-id}
                  :token jwt})))})

(def projects
  {:target [:edb/collection :project/list]
   :deps [:jwt]
   :loader gql/loader
   :params (fn [_ {:keys [page subpage id]} {:keys [jwt]}]
             (when (and (= "organizations" page) (= "view" subpage)
                        id jwt)
               {:query [:fetch-organization-projects 
                        [:fetchOrganization :projects]]
                :variables {:id id}
                :token jwt}))})

(def current-project
  {:target [:edb/named-item :project/current]
   :deps [:jwt :current-flow]
   :loader gql/loader
   :params (fn [_ {:keys [page id project-id]} {:keys [jwt current-flow]}]
             (let [project-id
                   (cond 
                     (and (= "projects" page) id) id
                     current-flow (get-in current-flow [:project :id])
                     :else project-id)]
               (when (and jwt project-id)
                 {:query [:fetch-project :fetchProject]
                  :variables {:id project-id}
                  :token jwt})))})

(def flows
  {:target [:edb/collection :flow/list]
   :deps [:jwt :current-project]
   :loader gql/loader
   :params (fn [_ {:keys [page subpage id project-id]} {:keys [jwt current-project current-flow]}]
             (let [project-id
                   (cond
                     (and (= "projects" page) (= "view" subpage) id) id
                     current-project (:id current-project)
                     current-flow (:projectId current-flow)
                     :else project-id)]
               (when (and project-id jwt)
                 {:query [:fetch-project-flows 
                          [:fetchProject :flows]]
                  :variables {:id project-id}
                  :token jwt})))})

(def current-flow
  {:target [:edb/named-item :flow/current]
   :deps [:jwt]
   :loader gql/loader
   :params (fn [_ {:keys [page id]} {:keys [jwt]}]
             (when (and (= "flows" page) id)
               {:query [:fetch-flow :fetchFlow]
                :variables {:id id}
                :token jwt}))})



(def datasources
  {:jwt                  jwt
   :current-account      current-account
   :organizations        organizations
   :current-organization current-organization
   :projects             projects
   :current-project      current-project
   :flows                flows
   :current-flow         current-flow})
