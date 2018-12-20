(ns protok.ui.login
  (:require [keechma.ui-component :as ui] 
            [keechma.toolbox.ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.svgs :refer [logo]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]))

(defelement -wrap
  :class [:flex :w100vw :h100vh :items-center :justify-center])

(defelement -inner-wrap
  :class [:rounded :bg-white :sh5 :overflow-hidden]
  :style {:max-width "400px"
          :width "90%"})

(defelement -logo-wrap
  :class [:flex :justify-center :bdb-neutral-8 :bwb1 :bwt5 :bdt-neutral-7])

(defelement -inner-logo-wrap
  :class [:py3]
  :style {:width "140px"})

(defelement -body-wrap
  :class [:p3 :fs1 :bg-neutral-9])

(defelement -forms-wrap)

(defn render-request-login-code-form [ctx]
  (let [form-props [:request-login-code :form]
        form-state (forms-ui/form-state> ctx form-props)
        state (get-in form-state [:state :type])]
    (when (not= :submitted state)
      [:form {:on-submit #(forms-ui/<submit ctx form-props %)}
       [inputs/text ctx form-props :email {:label "Your Email" :placeholder "email@example.com"}]
       [:div.flex.justify-end
        [buttons/secondary-small
         {:button/pill true
          :icon/right :email}
         "Request Login Link"]]])))

(defn render-login-with-code-form [ctx]
  (let [form-props [:login-with-code :form]
        form-state (forms-ui/form-state> ctx form-props)]
    (when (forms-ui/value-in> ctx form-props :email)
      [:form {:on-submit #(forms-ui/<submit ctx form-props %)}
       [inputs/text ctx form-props :email {:label "Your Email" :placeholder "email@example.com" :disabled true}]
       [inputs/text ctx form-props :code {:placeholder "Example: x1f35mg9aWidJ23" :label "Enter code"}]
       [:div.flex.justify-end
        [buttons/link-small
         {:on-click (fn [e]
                      (.preventDefault e)
                      (forms-ui/<call ctx form-props :reset-login-flow))}
         "Request new code"]
        [buttons/primary-small
         {:button/pill true
          :icon/right :arrow_forward}
         "Login"]]])))

(defn render [ctx]
  [-wrap
   [-inner-wrap
    [-logo-wrap
     [-inner-logo-wrap [logo]]]
    [-body-wrap
     [:div.center.pb3
      [:span.c-neutral-2 "Enter your email to sign up or sign in"]
      [:br]
      [:i.c-neutral-5.fs0 "(We'll send you the login link via email)"]]
     [-forms-wrap
      [render-request-login-code-form ctx]
      [render-login-with-code-form ctx]]]]])

(def component
  (ui/constructor {:renderer render}))
