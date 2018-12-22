(ns protok.domain.gql
  (:require [clojure.walk :refer [prewalk]]
            [camel-snake-kebab.core :refer [->kebab-case]]))

(defn process-account-role [data]
  (prewalk
   (fn [value]
     (if (and (map? value)
              (string? (:account/role value)))
       (update value :account/role #(keyword :role (->kebab-case %)))
       value))
   data))

(defn mutate-organization-processor [organization]
  (-> organization
      (dissoc :membership)
      (assoc :account/role (get-in organization [:membership :memberRole]))
      process-account-role))
