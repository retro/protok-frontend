(ns protok.ui
  (:require [protok.ui.main :as main]
            [protok.ui.editor :as editor]
            [protok.ui.login :as login]))

(def ui
  {:main   main/component
   :editor editor/component
   :login  login/component})
