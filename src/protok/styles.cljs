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

(defn stylesheet []
  [(reset/stylesheet) 
   (helpers/stylesheet)
   (typography/stylesheet)
   (core/stylesheet)
   (colors/stylesheet)
   @css/component-styles
   [:html {:height "100%"
           :font-size "16px" 
           :-webkit-font-smoothing "antialiased"
           :font-family system-font-stack}]
   [:body {:height "100%"}]])










