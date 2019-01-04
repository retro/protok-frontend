(ns protok.edb
  (:require [entitydb.core]
            [keechma.toolbox.edb :refer-macros [defentitydb]]))

(def edb-schema
  {:flow-node {:id :id
               :relations {:projectFile [:one :project-file]}}
   :flow {:id :id
          :relations {:flowNodes [:many :flow-node]}}
   :project {:id :id
             :relations {:projectFiles [:many :project-file]}}
   :project-file {:id :id}})

(defentitydb edb-schema)
