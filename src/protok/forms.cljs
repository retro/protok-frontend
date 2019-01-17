(ns protok.forms
  (:require [protok.forms.login-with-code :as login-with-code]
            [protok.forms.request-login-code :as request-login-code]
            [protok.forms.organization :as organization]
            [protok.forms.project :as project]
            [protok.forms.flow :as flow]
            [protok.forms.flow-event :as flow-event]
            [protok.forms.flow-flow-ref :as flow-flow-ref]
            [protok.forms.flow-screen :as flow-screen]
            [protok.forms.flow-switch :as flow-switch]
            [protok.forms.invite-organization-member :as invite-organization-member]
            [protok.domain.form-ids :as form-ids]
            [protok.util.forms :refer [provide]]))


(def forms
  {:request-login-code
   (provide request-login-code/constructor form-ids/request-login-code)

   :login-with-code
   (provide login-with-code/constructor)
   
   :organization
   (provide organization/constructor form-ids/organization)

   :project
   (provide project/constructor form-ids/project)

   :flow
   (provide flow/constructor form-ids/flow)

   :flow-event
   (provide flow-event/constructor)

   :flow-flow-ref
   (provide flow-flow-ref/constructor)

   :flow-screen
   (provide flow-screen/constructor)

   :flow-switch
   (provide flow-switch/constructor)

   :invite-organization-member
   (provide invite-organization-member/constructor form-ids/invite-organization-member)})
