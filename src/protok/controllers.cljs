(ns protok.controllers
  (:require [protok.controllers.initializer]))

(def controllers
  (-> {}
      (protok.controllers.initializer/register)))
