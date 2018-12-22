(ns protok.util.forms)

(defn provide
  ([form] (provide form (constantly nil)))
  ([form auto-mount-fn]
   {:form (if (fn? form) (form) form)
    :auto-mount-fn auto-mount-fn}))

(defn auto-mount-fns [forms]
  (into {} (map (fn [[k form]] [k (:auto-mount-fn form)]) forms)))

(defn forms [forms]
  (into {} (map (fn [[k form]] [k (:form form)]) forms)))
