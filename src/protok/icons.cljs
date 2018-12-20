(ns protok.icons
  (:require-macros [protok.icons :refer [inline-icon]]))

(def icons
  {:email (inline-icon :email)
   :arrow-forward (inline-icon :arrow-forward)})

(defn icon [i]
  (let [val (icons i)]
    (if val
      val
      (js/console.error (str "Missing icon " i)))))
