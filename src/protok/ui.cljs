(ns protok.ui
  (:require [protok.ui.main :as main]
            [protok.ui.editor :as editor]
            [protok.ui.login :as login]
            [protok.ui.not-found :as not-found]
            [protok.ui.organizations.list :as organizations-list]
            [protok.ui.organizations.form :as organizations-form]
            [protok.ui.projects.list :as projects-list]
            [protok.ui.projects.form :as projects-form]
            [protok.ui.flows.list :as flows-list]
            [protok.ui.flows.form :as flows-form]
            [protok.ui.flows.editor :as flows-editor]
            [protok.ui.flows.editor.node-form :as flows-editor-node-form]
            [protok.ui.loading :as loading]
            [protok.ui.components.layout :as layout]
            [protok.ui.components.content-layout :as content-layout]))

(def ui
  {:main                     main/component
   :editor                   editor/component
   :login                    login/component
   :loading                  loading/component
   :not-found                not-found/component
   :organizations/list       organizations-list/component
   :organizations/form       organizations-form/component
   :projects/list            projects-list/component
   :projects/form            projects-form/component
   :flows/list               flows-list/component
   :flows/form               flows-form/component
   :flows/editor             flows-editor/component
   :flows/node-form          flows-editor-node-form/component
   :component/layout         layout/component
   :component/content-layout content-layout/component})
