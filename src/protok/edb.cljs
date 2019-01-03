(ns protok.edb
  (:require [entitydb.core]
            [keechma.toolbox.edb :refer-macros [defentitydb]]))

(def edb-schema
  {:flow-node {:id :id}
   :flow {:id :id
          :relations {:flowNodes [:many :flow-node]}}})

(defentitydb edb-schema)
