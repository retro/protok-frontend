(ns protok.ui
  (:require [protok.ui.main :as main]
            [protok.ui.editor :as editor]
            [protok.ui.login :as login]
            [protok.ui.organizations :as organizations]
            [protok.ui.organizations.list :as organizations-list]
            [protok.ui.loading :as loading]
            [protok.ui.components.layout :as layout]))

(def ui
  {:main               main/component
   :editor             editor/component
   :login              login/component
   :organizations      organizations/component
   :organizations/list organizations-list/component
   :loading            loading/component
   :component/layout   layout/component})
