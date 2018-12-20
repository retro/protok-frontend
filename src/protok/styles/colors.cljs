(ns protok.styles.colors
  (:require [garden.color :as color]))

(def palette
  {:blue    ["#035388" "#40C3F7" "#0B69A3" "#5ED0FA" "#127FBF" "#81DEFD" "#1992D4" "#B3ECFF" "#2BB0ED" "#E3F8FF"]
   :neutral ["#1F2933" "#7B8794" "#323F4B" "#9AA5B1" "#3E4C59" "#CBD2D9" "#52606D" "#E4E7EB" "#616E7C" "#F5F7FA"]
   :pink    ["#620042" "#870557" "#A30664" "#BC0A6F" "#DA127D" "#E8368F" "#F364A2" "#FF8CBA" "#FFB8D2" "#FFE3EC"]
   :red     ["#610316" "#8A041A" "#AB091E" "#CF1124" "#E12D39" "#EF4E4E" "#F86A6A" "#FF9B9B" "#FFBDBD" "#FFE3E3"]
   :yellow  ["#8D2B0B" "#B44D12" "#CB6E17" "#DE911D" "#F0B429" "#F7C948" "#FADB5F" "#FCE588" "#FFF3C4" "#FFFBEA"]
   :green   ["#014D40" "#0C6B58" "#147D64" "#199473" "#27AB83" "#3EBD93" "#65D6AD" "#8EEDC7" "#C6F7E2" "#EFFCF6"]})

(defn sort-colors-by-lightness [colors]
  (vec (sort-by 
        (fn [c] 
          (-> (color/hex->rgb c)
              color/rgb->hsl
              :lightness))
        colors)))

(def named-colors
  (reduce-kv
   (fn [m k v]
     (let [sorted-v (sort-colors-by-lightness v)
           base (name k)
           named (into {} (map-indexed (fn [i c] [(keyword (str base "-" i)) c]) sorted-v))]
       (merge m named)))
   {}
   palette))

(def colors
  (merge {:white "#ffffff"
          :black "#000000"}
         named-colors))

(defn transition [prop]
  (str (name prop) " 0.10s ease-in-out"))

(defn gen-colors-styles [class-name prop]
  (map (fn [[color-name val]]
         (let [color-name (name color-name)
               normal-class (str "." class-name "-" color-name)
               hover-class (str "." class-name "-h-" color-name)
               hover ":hover"
               make-important #(str %1 " !important")]
           [[normal-class {prop val}]
            [(str hover-class hover) {prop val}]

            [(str normal-class "-i") {prop (make-important val)}]
            [(str hover-class "-i" hover) {prop (make-important val)}]])) colors))

(defn stylesheet []
  [[:.bg-transparent {:background "transparent"}]
   (gen-colors-styles "bg" :background-color)
   (gen-colors-styles "c" :color)
   (gen-colors-styles "f" :fill)
   (gen-colors-styles "bd" :border-color)
   (gen-colors-styles "bdt" :border-top-color)
   (gen-colors-styles "bdb" :border-bottom-color)
   (gen-colors-styles "bdl" :border-left-color)
   (gen-colors-styles "bdl" :border-right-color)
   [:.t-c {:transition (transition :color)}]
   [:.t-bg {:transition (transition :background-color)}]
   [:.t-bd {:transition (transition :border-color)}]])
