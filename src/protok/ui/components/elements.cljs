(ns protok.ui.components.elements
  (:require [keechma.toolbox.css.core :refer-macros [defelement]]))

(defelement -layout
  :style [{:max-width "960px"
           :margin "0 auto"}])

(defelement -page-title
  :tag :h1
  :class [:mt3 :mb2 :fs5 :c-neutral-2]
  :style [{:text-transform "uppercase"
           :letter-spacing "0.02em"
           :text-shadow "0px 1px 0 rgba(255,255,255,0.7)"}])

(defelement -content-wrap
  :class [:bg-white :rounded :sh1 :overflow-hidden])

(defelement -breadcrumbs-wrap
  :tag :ul
  :class [:mt3]
  :style [[:li {:display "inline-block"
                :margin-right "10px"}]])
