(ns protok.controllers
  (:require [protok.controllers.initializer]
            [protok.controllers.user-actions]
            [protok.controllers.kv]
            [protok.controllers.node-form-mounter]
            [protok.controllers.image-uploader]))

(def controllers
  (-> {}
      (protok.controllers.initializer/register)
      (protok.controllers.user-actions/register)
      (protok.controllers.kv/register)
      (protok.controllers.node-form-mounter/register)
      (protok.controllers.image-uploader/register)))
