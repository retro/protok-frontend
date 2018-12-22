(ns protok.datasources
  (:require [protok.settings :refer [jwt-ls-name]]
            [protok.util.local-storage :refer [ls-get]]
            [keechma.toolbox.dataloader.subscriptions :refer [map-loader]]
            [protok.gql :as gql]))

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
  {:target [:edb/collection :organizations/list]
   :deps [:jwt]
   :loader gql/loader
   :processor (fn [data]
                (map
                 (fn [o]
                   (-> (:organization o)
                       (assoc :account/role (:member-role o))))
                 data))
   :params (fn [_ _ {:keys [jwt]}]
             (when jwt
               {:query [:organization-memberships
                        [:currentAccount :organizationMemberships]]
                :token jwt}))})

(def datasources
  {:jwt             jwt
   :current-account current-account
   :organizations   organizations})
