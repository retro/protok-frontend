(ns protok.domain.db)

(defn make-get-in-kv [path])
(defn make-assoc-in-kv [path])
(defn make-dissoc-in-kv [path])

(defmacro defkvaccess [& attrs]
  (let [defs (mapv
                (fn [attr]
                  (let [[base path]
                        (if (keyword? attr)
                          [(name attr) attr]
                          [(name (first attr)) (second attr)])
                        dissoc-name (symbol (str "dissoc-" base))
                        assoc-name (symbol (str "assoc-" base))
                        get-name (symbol (str "get-" base))]
                    
                    [`(def ~dissoc-name (protok.domain.db/make-dissoc-in-kv ~path))
                     `(def ~get-name (protok.domain.db/make-get-in-kv ~path))
                     `(def ~assoc-name (protok.domain.db/make-assoc-in-kv ~path))]))
                attrs)]
    `(do ~@(apply concat defs))))
