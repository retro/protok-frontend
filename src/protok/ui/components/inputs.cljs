(ns protok.ui.components.inputs
  (:require [keechma.toolbox.forms.helpers :as forms-helpers]
            [protok.forms.validators :as validators]
            [reagent.core :as r]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.styles.colors :refer [colors]]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [keechma.toolbox.util :refer [class-names]]
            [clojure.string :as str]
            [oops.core :refer [oget]]))

(def text-inputs-class
  [:block :w100p :rounded :bd-neutral-7 :bw1 :c-neutral-2])

(def text-inputs-styles
  [{:padding "5px 9px"
    :box-shadow "0px 1px 0px white, 0px 2px 5px rgba(0,0,0,0.07) inset"
    :outline "none"
    :transition "border-color 0.15s ease-in-out"}
   [:&.size-normal
    {:padding "5px 9px"}]
   [:&.size-small
    {:padding "3px 7px"}]
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
  :style text-inputs-styles)

(defelement -text-area
  :tag :textarea
  :class text-inputs-class
  :style text-inputs-styles)

(defelement -label
  :tag :label
  :class [:block :fs1 :c-neutral-6 :bold]
  :style [{:padding-bottom "5px"
           :transition "all 0.15s ease-in-out"}
          [:&.has-errors {:color (colors :red-6)}]])

(defelement -select
  :tag :select
  :class [:block :w100p :rounded :bd-neutral-7 :bw1 :c-neutral-2 :bg-neutral-9]
  :style [{:-moz-appearance "none"
           :-webkit-appearance "none"
           :appearance "none"
           :background-image "url('data:image/svg+xml;charset=US-ASCII,%3Csvg%20xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22%20width%3D%22292.4%22%20height%3D%22292.4%22%3E%3Cpath%20fill%3D%22%23007CB2%22%20d%3D%22M287%2069.4a17.6%2017.6%200%200%200-13-5.4H18.4c-5%200-9.3%201.8-12.9%205.4A17.6%2017.6%200%200%200%200%2082.2c0%205%201.8%209.3%205.4%2012.9l128%20127.9c3.6%203.6%207.8%205.4%2012.8%205.4s9.2-1.8%2012.8-5.4L287%2095c3.5-3.5%205.4-7.8%205.4-12.8%200-5-1.9-9.2-5.5-12.8z%22%2F%3E%3C%2Fsvg%3E')"
           :background-repeat "no-repeat"
           :background-position "right .7em top 50%"
           :background-size ".65em"}
          [:&.size-normal
           {:height "39px"
            :text-indent "9px"}]
          [:&.size-small
           {:height "29px"
            :text-indent "7px"}]
          [:&::-ms-expand {:display "none"}]
          [:&:hover {:border-color (colors :neutral-6)}]
          [:&:focus 
           {:border-color (colors :blue-5)
            :outline "none"}]
          [:&.has-errors
           {:color (colors :red-3)
            :border-color (colors :red-8)}
           [:&:focus {:border-color (colors :red-4)}]]])

(defelement -error-messages-wrap
  :tag :ul
  :class [:c-red-5 :fs1]
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

(defn process-classes [classes]
  (str/join " " (map name (filter (complement nil?) (flatten [classes])))))

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

(defn text [ctx form-props attr {:keys [input-type class] :as input-props}]
  (let [errors (forms-ui/errors-in> ctx form-props attr)
        input-size (or (:input/size input-props) :normal)]
    [-fieldset
     [render-label input-props errors]
     [input-with-composition-support
      (-> {:placeholder (get-placeholder input-props)
           :on-change #(forms-ui/<on-change ctx form-props attr %)
           :on-blur #(forms-ui/<on-blur ctx form-props attr %)
           :value (forms-ui/value-in> ctx form-props attr)
           :type (or input-type :text)
           :class (class-names {:has-errors (seq errors)
                                "fs2 size-normal" (= :normal input-size)
                                "fs1 size-small" (= :small input-size)
                                (process-classes class) true})}
          (merge (select-keys input-props [:auto-focus :disabled])))]
     [render-errors errors]]))

(defn textarea [ctx form-props attr {:keys [rows class] :as input-props}]
  (let [errors (forms-ui/errors-in> ctx form-props attr)
        input-size (or (:input/size input-props) :normal)]
    [-fieldset
     [render-label input-props]
     [textarea-with-composition-support
      {:placeholder (get-placeholder input-props)
       :rows (or rows 6)
       :on-change #(forms-ui/<on-change ctx form-props attr %)
       :on-blur #(forms-ui/<on-blur ctx form-props attr %)
       :value (forms-ui/value-in> ctx form-props attr)
       :style {:resize "vertical"}
       :class (class-names {:has-errors (seq errors)
                            "fs2 size-normal" (= :normal input-size)
                            "fs1 size-small" (= :small input-size)
                            (process-classes class) true})}]
     [render-errors (forms-ui/errors-in> ctx form-props attr)]]))

(defn select [ctx form-props attr {:keys [options optgroups class] :as input-props}]
  (let [errors (forms-ui/errors-in> ctx form-props attr)
        placeholder (get-placeholder input-props)
        input-size (or (:input/size input-props) :normal)]
    [-fieldset
     [render-label input-props]
     [-select
      {:on-change #(forms-ui/<on-change ctx form-props attr %)
       :value (or (forms-ui/value-in> ctx form-props attr) "")
       :class (class-names {:has-errors (seq errors)
                            "fs2 size-normal" (= :normal input-size)
                            "fs1 size-small" (= :small input-size)
                            (process-classes class) true})}
      [:option {:value ""} placeholder]
      [:option {:value ""} "—"]
      (if optgroups
        (map 
         (fn [{:keys [label options]}]
           (when (seq options)
             [:optgroup {:label label :key label}
              (map 
               (fn [{:keys [value label]}]
                 [:option 
                  {:value value 
                   :key value} 
                  label]) 
               (sort-by :label options))]))
         (sort-by :label optgroups))
        (map 
         (fn [{:keys [value label]}]
           [:option 
            {:value value 
             :key value} 
            label]) 
         (sort-by :label options)))]
     [render-errors (forms-ui/errors-in> ctx form-props attr)]]))

(defn checkbox [ctx form-props attr {:keys [class] :as input-props}]
  (let [errors (forms-ui/errors-in> ctx form-props attr)
        input-size (or (:input/size input-props) :normal)]
    [-fieldset
     [:label.fs2.c-neutral-3.block
      [:input.mr1
       {:placeholder (get-placeholder input-props)
        :on-change #(forms-ui/<set-value ctx form-props attr (oget % :target.checked))
        :checked (boolean (forms-ui/value-in> ctx form-props attr))
        :type "checkbox"
        :class (class-names {:has-errors (seq errors)
                             (process-classes class) true})}]
      (get-label input-props)]
     [render-errors (forms-ui/errors-in> ctx form-props attr)]]))
