(ns protok.controllers
  (:require [protok.controllers.initializer]
            [protok.controllers.user-actions]
            [protok.controllers.kv]))

(def controllers
  (-> {}
      (protok.controllers.initializer/register)
      (protok.controllers.user-actions/register)
      (protok.controllers.kv/register)))
