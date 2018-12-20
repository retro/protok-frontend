(ns protok.styles.typography)

(def font-sizes [12 14 16 18 20 24 30 36 48 60 72])
(def line-heights {12 1.5
                   14 1.5
                   16 1.5})

(defn generate-font-sizes []
  (map-indexed
   (fn [idx fs]
     [(keyword (str ".fs" idx))
      {:font-size (str (/ fs 16) "rem")
       :line-height (or (line-heights fs) 1)}])
   font-sizes))

(defn stylesheet []
  (generate-font-sizes))
