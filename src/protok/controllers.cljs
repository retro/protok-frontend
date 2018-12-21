(ns protok.controllers
  (:require [protok.controllers.initializer]
            [protok.controllers.user-actions]))

(def controllers
  (-> {}
      (protok.controllers.initializer/register)
      (protok.controllers.user-actions/register)))
