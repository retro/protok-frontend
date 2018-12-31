(ns protok.forms
  (:require [protok.forms.login-with-code :as login-with-code]
            [protok.forms.request-login-code :as request-login-code]
            [protok.forms.organization :as organization]
            [protok.forms.project :as project]
            [protok.forms.flow :as flow]
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
   (provide flow/constructor form-ids/flow)})
