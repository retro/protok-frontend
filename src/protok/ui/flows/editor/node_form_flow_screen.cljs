(ns protok.ui.flows.editor.node-form-flow-screen
  (:require [keechma.ui-component :as ui] 
            [keechma.toolbox.ui]
            [keechma.toolbox.css.core :refer-macros [defelement]]
            [protok.ui.components.inputs :as inputs]
            [keechma.toolbox.forms.helpers :as forms-helpers]
            [keechma.toolbox.forms.core :as forms-core]
            [keechma.toolbox.forms.ui :as forms-ui]
            [protok.ui.components.buttons :as buttons]
            [protok.domain.form-ids :as form-ids]
            [keechma.toolbox.ui :refer [<cmd route> sub>]]
            [protok.ui.shared :refer [<submit-exclusive]]
            [oops.core :refer [oget]]
            [protok.domain.project-files :as project-files]
            [protok.ui.flows.editor.shared :refer [node-type-name]]
            [protok.ui.flows.editor.node-form-shared :refer [render-hotspots-options -form-subtitle]]
            [protok.icons :refer [icon]]
            [protok.styles.colors :refer [colors]]))

(def checkers-bg
  "url(\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 10 10'%3E%3Cg fill='%23000000' fill-opacity='0.09'%3E%3Cpath fill-rule='evenodd' d='M0 0h5v5H0V0zm5 5h5v5H5V5z'/%3E%3C/g%3E%3C/svg%3E\")")

(defelement -img-progress-wrap
  :class [:absolute]
  :style [{:top "10px"
           :right "10px"
           :width "54px"
           :height "14px"
           :padding "2px"
           :border-radius "7px"
           :background-color "rgba(0,0,0,0.5)"}])

(defelement -img-progress-inner-wrap
  :class [:overflow-hidden]
  :style [{:width "50px"
           :height "10px"
           :border-radius "5px"
           :border "2px solid white"}])

(defelement -img-progress
  :style [{:background-color "white"
           :height "6px"}])

(defelement -screen-img-wrap
  :class [:mxn2 :mb2 :p3 :bd-neutral-8 :bwt1 :bwb1 :relative]
  :style [{:background-image checkers-bg
           :background-repeat "repeat"}])

(defelement -screen-img
  :tag :img
  :class [:mx-auto :block]
  :style [{:max-height "300px"
           :max-width "100%"}])

(defelement -uploader-wrap
  :class [:relative :pointer :rounded :mb2]
  :style [{:height "40px"}])

(defelement -dropzone-wrap
  :tag :label
  :class [:overflow-hidden
          :rounded 
          :pointer 
          :absolute
          :top-0
          :left-0
          :right-0
          :bottom-0
          :flex
          :justify-center
          :items-center
          :bg-neutral-9
          :bd-neutral-7
          :bd-h-blue-5
          :bw1
          :c-neutral-2
          :fs2]
  :style [{:transition "all .15s ease-in-out"}])

(defelement -dropzone-input
  :tag :input
  :class [:block :absolute :pointer :top-0 :right-0 :left-0 :bottom-0]
  :style [{:min-width "100%"
           :opacity 0
           :text-align "right"
           :min-height "100%"
           :font-size "999px"}])

(defelement -add-photo-icon-wrap
  :class [:inline-block :relative]
  :style [{:top "3px"
           :margin-right "5px"}
          [:svg {:height "18px"
                 :width "18px"
                 :fill (colors :neutral-4)}]])

(defn render [ctx form-props]
  (let [form-state (forms-ui/form-state> ctx form-props)
        hotspots (or (forms-ui/value-in> ctx form-props :hotspots) [])
        project-file (forms-ui/value-in> ctx form-props :projectFile)
        progress (:protok/progress (sub> ctx :project-file-by-id (:id project-file)))]
    [:<>
     [inputs/text ctx form-props :name 
      {:label "Name"
       :placeholder "Name"}]
     [inputs/textarea ctx form-props :description
      {:label "Description"}]
     (when project-file
       [-screen-img-wrap
        [-screen-img {:src (project-files/url project-file)}]
        (when (and progress (not= 1 progress))
          [-img-progress-wrap
           [-img-progress-inner-wrap
            [-img-progress {:style {:width (str (* 100 progress) "%")}}]]])])
     [-uploader-wrap
      [-dropzone-wrap
       [-dropzone-input
        {:type :file 
         :accept ".jpg,.jpeg,.png,.gif,image/jpeg,image/gif,image/png"
         :multiple false
         :on-change #(<cmd ctx [:image-uploader :upload] {:file (oget % :target.files.0) :form-props form-props :path [:projectFile]})}]
       [:div
        [-add-photo-icon-wrap
         (icon :add-photo-alternate)]
        "Select an image to upload (or drag and drop it here)"]]]
     [:div.bwt1.bd-neutral-7.mt2.pt2
      [-form-subtitle "Hotspots"]
      [:div
       (map-indexed
        (fn [idx o]
          ^{:key idx}
          [render-hotspots-options ctx form-props hotspots :hotspots idx])
        hotspots)]
      [:div.flex.justify-end
       [buttons/secondary-small
        {:on-click #(forms-ui/<set-value ctx form-props :hotspots (conj hotspots {}))
         :type :button
         :button/pill true}
        "Add Hotspot"]]]]))
