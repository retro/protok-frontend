(ns protok.ui.components.inputs
  (:require [keechma.toolbox.forms.helpers :as forms-helpers]
            [protok.forms.validators :as validators]
            [reagent.core :as r]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.styles.colors :refer [colors]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.util :refer [class-names]]))

(def text-inputs-class
  [:block :fs1 :w100p :rounded :bd-neutral-7 :bw1 :c-neutral-2])
(def text-inputs-style
  [{:padding "5px 9px"
    :box-shadow "0px 1px 0px white, 0px 2px 5px rgba(0,0,0,0.07) inset"
    :outline "none"
    :transition "border-color 0.15s ease-in-out"}
   [:&::placeholder {:color (colors :neutral-6)}]
   [:&:focus {:border-color (colors :blue-5)}]
   [:&.has-errors
    {:color (colors :red-3)
     :border-color (colors :red-8)}
    [:&:focus {:border-color (colors :red-4)}]]
   [:&:disabled
    {:opacity 0.7
     :cursor "not-allowed"
     :background-color "rgba(0,0,0,.03)"}]])

(defelement -fieldset
  :tag :fieldset
  :class [:mb2]
  :style [[:&:focus-within
           [:.protok_ui_components_inputs--label
            {:color (colors :blue-3)}]
           [:.protok_ui_components_inputs--label.has-errors
            {:color (colors :red-4)}]]])

(defelement -text-input
  :tag :input
  :class text-inputs-class
  :style text-inputs-style)

(defelement -text-area
  :tag :textarea
  :class text-inputs-class
  :style text-inputs-style)

(defelement -label
  :tag :label
  :class [:block :fs0 :c-neutral-6 :bold]
  :style [{:padding-bottom "5px"
           :transition "all 0.15s ease-in-out"}
          [:&.has-errors {:color (colors :red-6)}]])

(defelement -select
  :tag :select)

(defelement -error-messages-wrap
  :tag :ul
  :class [:c-red-5 :fs0]
  :style {:padding-top "5px"})

(defn make-input-with-composition-support [tag]
  ;; This function implements input fields that handle composition based inputs correctly
  (fn [props]
    (let [el-ref-atom (atom nil)
          composition-atom? (atom false)]
      (r/create-class
       {:reagent-render (fn [props]
                          (let [props-ref  (or (:ref props) identity)
                                props-on-change (or (:on-change props) identity)
                                props-value (:value props)
                                props-without-value (dissoc props :value)]
                            [tag (merge props-without-value
                                        {:on-change (fn [e]
                                                      (when-not @composition-atom?
                                                        (props-on-change e)))
                                         :on-composition-start #(reset! composition-atom? true)
                                         :on-composition-update #(reset! composition-atom? true)
                                         :on-composition-end (fn [e]
                                                               (reset! composition-atom? false)
                                                               (props-on-change e))
                                         :default-value props-value
                                         :ref (fn [el]
                                                (reset! el-ref-atom el)
                                                (props-ref el))})]))
        :component-will-update (fn [comp [_ new-props _]]
                                 (let [el @el-ref-atom
                                       composition? @composition-atom?]
                                   (when (and el (not composition?))
                                     (set! (.-value el) (or (:value new-props) "")))))}))))

(def input-with-composition-support (make-input-with-composition-support -text-input))
(def textarea-with-composition-support (make-input-with-composition-support -text-area))

(defn render-errors [attr-errors]
  (when-let [errors (get-in attr-errors [:$errors$ :failed])]
    (into [-error-messages-wrap]
          (doall (map (fn [e]
                        [:li (validators/get-validator-message e)])
                      errors)))))

(defn get-placeholder [{:keys [placeholder label]}]
  (or placeholder label))

(defn get-label [{:keys [label placeholder]}]
  (or label placeholder))

(defn render-label [input-props errors]
  [-label {:class (class-names {:has-errors (seq errors)})}
   (get-label input-props)])

(defn text [ctx form-props attr {:keys [input-type] :as input-props}]
  (let [errors (forms-ui/errors-in> ctx form-props attr)]
    [-fieldset
     [render-label input-props errors]
     [input-with-composition-support
      (-> {:placeholder (get-placeholder input-props)
           :on-change #(forms-ui/<on-change ctx form-props attr %)
           :on-blur #(forms-ui/<on-blur ctx form-props attr %)
           :value (forms-ui/value-in> ctx form-props attr)
           :type (or input-type :text)
           :class (class-names {:has-errors (seq errors)})}
          (merge (select-keys input-props [:auto-focus :disabled])))]
     [render-errors errors]]))

(defn textarea [ctx form-props attr {:keys [rows] :as input-props}]
  [-fieldset
   [render-label input-props]
   [textarea-with-composition-support
    {:placeholder (get-placeholder input-props)
     :rows (or rows 8)
     :on-change #(forms-ui/<on-change ctx form-props attr %)
     :on-blur #(forms-ui/<on-blur ctx form-props attr %)
     :value (forms-ui/value-in> ctx form-props attr)}]
   [render-errors (forms-ui/errors-in> ctx form-props attr)]])

(defn select [ctx form-props attr {:keys [options] :as input-props}]
  [-fieldset
   [render-label input-props]
   [-select
    {:on-change #(forms-ui/<on-change ctx form-props attr %)
     :value (or (forms-ui/value-in> ctx form-props attr) "")}
    [:option {:value ""} (get-placeholder input-props)]
    (doall (map (fn [[value label]]
                  [:option {:value value :key value} label]) options))]
   [render-errors (forms-ui/errors-in> ctx form-props attr)]])
