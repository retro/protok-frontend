(ns protok.styles.helpers
  (:require [garden-basscss.vars :refer [vars]]
            [garden.stylesheet :refer [at-media]]
            [garden.units :refer [px rem em]]))

(def breakpoints (:breakpoints @vars))

(defn at-screen [screen-size & args]
  (if (keyword? screen-size)
    (at-media (screen-size breakpoints) args)
    (at-media screen-size args)))
 
(defn stylesheet []
  [[:.pointer {:cursor "pointer"}]
   [:.uppercase {:text-transform "uppercase"}]
   [:.w100p {:width "100%"}]
   [:.h100p {:height "100%"}]
   [:.w100vw {:width "100vw"}]
   [:.h100vh {:height "100vh"}]])
