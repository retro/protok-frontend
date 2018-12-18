(ns keechma.toolbox.entangled.ui
  (:require [keechma.ui-component :as ui]
            [keechma.toolbox.ui :refer [sub> <cmd]]
            [reagent.core :as r]
            [cljs.core.async :refer [<! chan]]
            [keechma.toolbox.entangled.shared :as shared :refer [id ->ComponentCommand]])
  (:import [goog.async Throttle])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def get-app-db-path shared/get-app-db-path)
(def get-id shared/get-id)
(def get-name shared/get-name)

(defn throttle [f interval]
  (let [t (Throttle. f interval)]
    (fn [& args] (.apply (.-fire t) t (to-array args)))))

(defn <comp-cmd
  ([ctx command] (<comp-cmd ctx command nil))
  ([ctx command args]
   (let [{:keys [component-name component-id]} (get ctx :keechma.toolbox.entangled/component)]
     (<cmd ctx [component-name command] (->ComponentCommand component-id args)))))

(defn <comp-reset! [ctx data]
  (<comp-cmd ctx :keechma.toolbox.entangled.actions/reset data))

(defn <comp-swap! [ctx & args]
  (<comp-cmd ctx :keechma.toolbox.entangled.actions/swap args))

(def controller-topic id)

(def subscription-error-msg
  "Subscriptions are unavailable in the `render` function. Use the `state` function to subscribe.")

(def send-command-error-msg
  "Sending commands from the `state` function is unavailable. Use the `render` function to send commands.")

(def redirect-error-msg
  "Redirecting from the `state` function is unavailable. Use the `render` function to redirect.")

(defn make-command-error-thrower []
  (let [c (chan)]
    (go-loop []
      (let [cmd (<! c)]
        (when c
          (throw (ex-info send-command-error-msg {})))))
    c))

(defn make-render-ctx [ctx component]
  (let [subs (:subscriptions ctx)
        processed-subs (reduce-kv (fn [m k v] (assoc m k #(throw (ex-info subscription-error-msg {})))) {} subs)]
    (assoc ctx
           :subscriptions processed-subs
           :keechma.toolbox.entangled/component component)))

(defn make-state-ctx [ctx component]
  (let [cmd-chan (make-command-error-thrower)]
    (assoc ctx 
           :keechma.toolbox.entangled/component component
           :commands-chan cmd-chan
           :redirect-fn #(throw (ex-info redirect-error-msg {})))))

(defn wrap-renderer [renderer state-provider]
  (fn [ctx & args]
    (let [component-id (keyword (gensym :keechma.toolbox.entangled.ui/component))
          component-name (get ctx :keechma.toolbox.entangled/name)
          component {:component-id component-id
                     :component-name component-name}
          render-ctx (make-render-ctx ctx component)
          state-ctx  (make-state-ctx ctx component)
          tracker$ (atom {})
          <throttled-comp-cmd (throttle <comp-cmd 16.667)]
      (r/create-class
       {:reagent-render
        (fn [& args]
          (let [local-state (sub> ctx id (get ctx :keechma.toolbox.entangled/name) component-id)
                s (state-provider state-ctx local-state args)
                tracker @tracker$]
            (if (:inited? tracker)
              (do
                (when (not= s (:last-state tracker))
                  (<throttled-comp-cmd render-ctx :on-state-change s))
                (when (not= args (:last-args tracker))
                  (<comp-cmd render-ctx :on-args args)))
              (do
                (swap! tracker$ assoc :inited? true)
                (<comp-cmd render-ctx :on-init s)
                (<comp-cmd render-ctx :on-args args)))
            (swap! tracker$ merge {:last-state s :args args}) 
            [renderer render-ctx s]))
        :component-will-unmount (fn [_ _ _] (<comp-cmd render-ctx :on-terminate (:last-state @tracker$)))}))))

(defn constructor
  ([component-definition] (constructor component-definition nil))
  ([component-definition actions]
   (let [{:keys [renderer state-provider]
          :or {state-provider (constantly nil)}} component-definition]
     (ui/constructor
      (-> component-definition
          (dissoc :state-provider)
          (assoc :renderer (wrap-renderer renderer state-provider)
                 :keechma.toolbox.entangled/actions actions
                 id true))))))
