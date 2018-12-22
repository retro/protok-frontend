(ns protok.domain.route
  (:require [protok.domain.db :as db]
            [keechma.toolbox.logging :as l]))

(defn process-route-data [route app-db]
  (let [current-account (db/get-current-account app-db)
        initialized?    (db/get-initialized? app-db)]
    (cond
      (not initialized?)    {:page "loading"}
      (not current-account) {:page "login"}
      :else                 route)))

(defn log-route [route-data processed-route-data]
  (if (= route-data processed-route-data)
    (do (l/group "Route")
        (l/pp route-data)
        (l/group-end))
    (do (l/group "Route / Processed Route")
        (l/pp route-data)
        (l/pp processed-route-data)
        (l/group-end))))

(defn processor [route app-db]
  (let [route-data (route :data)
        processed-route-data (process-route-data route-data app-db)]
    (log-route route-data processed-route-data)
    (if (not= route-data processed-route-data)
        (assoc route
               :data processed-route-data
               :original-data route-data)
        route)))
