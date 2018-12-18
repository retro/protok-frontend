(ns protok.edb
  (:require [entitydb.core]
            [keechma.toolbox.edb :refer-macros [defentitydb]]))

(def edb-schema {})

(defentitydb edb-schema)
