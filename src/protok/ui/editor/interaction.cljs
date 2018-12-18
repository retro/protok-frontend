(ns protok.ui.editor.interaction
  (:require [oops.core :refer [oget ocall oget+]]
            [cljs.core.async :refer [put! <! chan]])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))


(defn get-mouse-coordinates [ev]
  {:x (oget ev :clientX)
   :y (oget ev :clientY)})

(def bounding-client-rect-props [:top :right :bottom :left :x :y :height :width])

(defn measure-element [el]
  (let [rect (ocall el :getBoundingClientRect)]
    (-> (into {} (map (fn [p] [p (oget+ rect p)]) bounding-client-rect-props))
        (assoc :scroll {:top (oget el :scrollLeft)
                        :left (oget el :scrollTop)}))))

(defn make-on-mouse-enter [ev-chan]
  #(put! ev-chan
         [:mouse-enter
          {:el (measure-element (oget % :target))
           :mouse (get-mouse-coordinates %)}]))

(defn make-on-mouse-leave [ev-chan]
  #(put! ev-chan [:mouse-leave]))

(defn make-on-mouse-move [ev-chan]
  #(put! ev-chan [:mouse-move {:mouse (get-mouse-coordinates %)}]))

(defn make-on-mouse-down [ev-chan]
  #(put! ev-chan [:mouse-down {:mouse-down (get-mouse-coordinates %)}]))

(defn make-on-mouse-up [ev-chan]
  #(put! ev-chan [:mouse-up {:mouse-up (get-mouse-coordinates %)}]))

(defn make-on-keydown [ev-chan]
  #(put! ev-chan [:key-down {}]))

(defn bind-event! [el ev handler]
  (let [wrapped-handler (fn [e]
                          (ocall e :preventDefault)
                          (handler e))]
    (ocall el :addEventListener (name ev) wrapped-handler)
    (fn []
      (ocall el :removeEventListener (name ev) wrapped-handler))))

(defn bind-events! [el ev-handler-map]
  (let [unbinders
        (doall 
         (map (fn [[ev handler]]
                (bind-event! el ev handler))
              ev-handler-map))]
    (fn []
      (doseq [u unbinders]
        (u)))))

(defn make-el-events-map [ev-chan]
  (->> {:mouseenter make-on-mouse-enter
        :mouseleave make-on-mouse-leave
        :mousemove  make-on-mouse-move
        :mousedown  make-on-mouse-down
        :mouseup    make-on-mouse-up}
       (map (fn [[ev make-handler]] [ev (make-handler ev-chan)]))
       (into {})))

(defn relativize-mouse-coordinates
  ([state] (relativize-mouse-coordinates state :mouse))
  ([state attr]
   (let [el-x (get-in state [:el :x])
         el-y (get-in state [:el :y])]
     (-> state
         (update-in [attr :x] - el-x)
         (update-in [attr :y] - el-y)))))

(defn handle-event [state ev payload]
  (case ev
    :mouse-enter
    (-> state
        (assoc :mouse-over? true)
        (merge payload)
        (relativize-mouse-coordinates))

    :mouse-move
    (-> state
        (merge payload)
        (relativize-mouse-coordinates))
    
    :mouse-down
    (-> state
        (merge payload)
        (dissoc :mouse-up)
        (relativize-mouse-coordinates :mouse-down))

    :mouse-up
    (-> state
        (merge payload)
        (dissoc :mouse-down)
        (relativize-mouse-coordinates :mouse-up))

    :mouse-leave
    (-> state
        (dissoc :mouse-up :mouse-down)
        (assoc :mouse-over? false))

    state))

(defn make-events-producer [el]
  (fn [res-chan _]
    (let [ev-chan (chan)
          el-events-map (make-el-events-map ev-chan)
          el-unbinders! (bind-events! el el-events-map)]

      (go-loop [last-ev nil
                state {}]
        (when (and state last-ev)
          (put! res-chan {:type last-ev :payload state}))
        (let [[ev payload] (<! ev-chan)]
          (when ev
            (recur ev (handle-event state ev payload)))))
      (fn []
        (el-unbinders!)))))
