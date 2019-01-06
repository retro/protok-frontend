(ns protok.util
  (:require [clojure.string :as str]
            [goog.crypt :refer [byteArrayToHex]])
  (:import goog.crypt.Md5))

(defn select-keys-by-namespace
  ([data] (select-keys-by-namespace data nil))
  ([data ns]
   (let [ns (keyword ns)]
     (reduce-kv (fn [m k v]
                  (let [key-ns (keyword (namespace k))]
                    (if (= key-ns ns)
                      (assoc m (keyword (name k)) v)
                      m))) {} data))))

(defn gravatar-url [email]
  (let [md5 (Md5.)]
    (.update md5 (str/trim (or email "")))
    (str "//www.gravatar.com/avatar/" (byteArrayToHex (.digest md5)) "?size=300&d=mm")))

(defn vec-remove
  "remove elem in coll"
  [coll pos]
  (vec (concat (subvec coll 0 pos) (subvec coll (inc pos)))))

