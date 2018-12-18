(ns protok.ui
  (:require [protok.ui.main :as main]
            [protok.ui.editor :as editor]))

(def ui
  {:main main/component
   :editor editor/component})
