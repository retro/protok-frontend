(ns protok.forms
  (:require [protok.forms.login-with-code :as login-with-code]
            [protok.forms.request-login-code :as request-login-code]))

(def forms {:request-login-code (request-login-code/constructor)
            :login-with-code    (login-with-code/constructor)})

(def forms-automount-fns {:request-login-code (fn [_]
                                                :form)})
