(ns keechma.toolbox.animations.ui
  (:require [keechma.toolbox.animations.shared :refer [id]]
            [keechma.toolbox.ui :refer [sub>]]))

(defn animation>
  ([ctx animation-id] (animation> ctx animation-id nil))
  ([ctx animation-id animation-version]
   (sub> ctx id animation-id animation-version)))
