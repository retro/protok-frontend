(ns protok.icons
  (:require [camel-snake-kebab.core :refer [->snake_case]]
            [pl.danieljanus.tagsoup :as ts]
            [clojure.walk :refer [prewalk]]))

(def rename-attr-mapping
  {:viewbox :viewBox})

(defn rename-attrs [hiccup]
  (prewalk
   (fn [v]
     (if-let [renamed (rename-attr-mapping v)]
       renamed
       v))
   hiccup))

(defmacro inline-icon [icon] 
  (let [filename (str (->snake_case (name icon)) ".svg")
        path (str "./resources/svg/icons/" filename)
        hiccup (rename-attrs (ts/parse-string (slurp path)))]
    `~hiccup))
