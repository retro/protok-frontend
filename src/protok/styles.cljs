(ns protok.styles
  (:require [keechma.toolbox.css.core :as css]
            [garden-basscss.core :as core]
            [protok.styles.reset :as reset]
            [protok.styles.typography :as typography]
            [protok.styles.colors :as colors]
            [protok.styles.helpers :as helpers]
            [clojure.string :as str]
            [garden.units :refer [px rem em]])
  (:require-macros [garden.def :refer [defkeyframes]]))

(def system-font-stack "system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI','Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', 'Fira Sans','Droid Sans', 'Helvetica Neue', sans-serif")

(defn generate-border-widths []
  (let [sizes (range 1 11)
        props {:border-width        :bw
               :border-top-width    :bwt
               :border-bottom-width :bwb
               :border-left-width   :bwl
               :border-right-width  :bwr}]
    (mapv
     (fn [s]
       (mapv
        (fn [[prop class-base]]
          (let [class-name (str "." (name class-base) s)
                style-prop (str/replace (name prop) #"-width$" "-style")]
            [class-name {prop (str s "px")
                         style-prop "solid"}]))
        props))
     sizes)))

(defn stylesheet []
  [(reset/stylesheet) 
   (helpers/stylesheet)
   (typography/stylesheet)
   (core/stylesheet)
   (colors/stylesheet)
   (generate-border-widths)
   @css/component-styles
   [:html {:height "100%"
           :font-size "16px" 
           :-webkit-font-smoothing "antialiased"
           :font-family system-font-stack}]
   [:body {:height "100%"
           :background (:neutral-8 colors/colors)}]
   [:.sh1 {:box-shadow "0 1px 3px rgba(0,0,0,.06), 0 1px 2px rgba(0,0,0,.12)"}]
   [:.sh2 {:box-shadow "0 3px 6px rgba(0,0,0,.075), 0 2px 4px rgba(0,0,0,.06)"}]
   [:.sh3 {:box-shadow "0 10px 20px rgba(0,0,0,.075), 0 3px 6px rgba(0,0,0,.05)"}]
   [:.sh4 {:box-shadow "0 15px 25px rgba(0,0,0,.075), 0 5px 10px rgba(0,0,0,.05)"}]
   [:.sh5 {:box-shadow "0 20px 50px rgba(0,0,0,.1)"}]
   [:.pill {:border-radius "999em"}]])










