(ns keechma.toolbox.animations.rn
  (:require [oops.core :refer [ocall+ ocall oget oget+ oapply+]]
            [cljs.core.async :refer [put! chan pipe close! <! timeout]]
            [keechma.toolbox.animations.core :as a]
            [keechma.toolbox.animations.helpers :as helpers]
            [keechma.toolbox.animations.animator :as animator]
            [keechma.toolbox.tasks :as t]
            [promesa.core :as p]
            [clojure.string :as str])
  (:require-macros [cljs.core.async.macros :refer [go-loop]]))

(def ReactNative (js/require "react-native"))

(def animated (oget ReactNative "Animated"))
(def AnimatedValue (oget ReactNative "Animated.Value"))
(def animated-event (oget ReactNative "Animated.event"))
(def PanResponder (oget ReactNative "PanResponder"))
(def easing (oget ReactNative "Easing"))

(defn dispatcher [meta & args]
  (:identifier meta))

(defn make-initial-meta
  ([identifier] (make-initial-meta identifier nil nil))
  ([identifier args] (make-initial-meta identifier args nil))
  ([identifier args prev]
   (let [[id state] (a/identifier->id-state identifier)]
     {:id id
      :state state
      :identifier identifier
      :position 0
      :times-invoked 0
      :prev (dissoc prev :prev)
      :args args})))

(defmulti config dispatcher)
(defmulti done? dispatcher)
(defmulti values dispatcher)

(defmethod config :default [meta _]
  (when js/goog.DEBUG
    (js/console.warn "Loading non existing animation config for: " (str (:identifier meta)) "/" (str (:state meta))))
  {:type :timing
   :config {:duration 10000}})

(defmethod done? :default [meta animator]
  (animator/done? animator))

(defmethod values :default [meta]
  {})

(defmulti pan-value dispatcher)
(defmulti pan-init-value dispatcher)
(defmulti pan-step dispatcher)

(defmethod pan-step :default [meta pan-value]
  {})

(defmethod pan-value :default [meta]
  [0 0])

(defmethod pan-init-value :default [meta]
  [0 0])

(defmulti scroll-config dispatcher)
(defmulti scroll-value dispatcher)

(defmethod scroll-config :default [_ _]
  {})

(defmethod scroll-value :default [meta scroll-info]
  0)


(defn get-from-value [config]
  (or (:fromValue config) 0))

(defn get-to-value [config]
  (or (:toValue config) 1))

(defn setup-easing [config]
  (if-let [e (:easing config)]
    (let [easing-values (:values e)
          easing-type (name (:type e))
          easing-fn (if easing-values
                      (oapply+ easing easing-type (clj->js easing-values))
                      (oget+ easing easing-type))]
      (assoc config :easing easing-fn))
    config))

(defn get-animated [animated-value {:keys [type config]}]
  (let [loop? (:loop? config)
        a (ocall+ animated (name type) animated-value
                  (clj->js (-> (merge {:useNativeDriver true :toValue 1} config)
                               setup-easing)))]
    (if loop?
      (ocall animated "loop" a)
      a)))

(defn make-animated-producer [config]
  (fn [res-chan _]
    (let [start-value (get-from-value config)
          value-atom (atom start-value)
          a-value (AnimatedValue. start-value)
          listener-id (ocall a-value "addListener" #(reset! value-atom (oget % "value")))
          a (get-animated a-value (dissoc config :fromValue))
          start (fn []
                  (ocall a "start"
                         (fn [& args]
                           (ocall a-value "removeListener" listener-id)
                           (put! res-chan {:animated a-value :done? true :value @value-atom}))))]
      (put! res-chan {:animated a-value :done? false :start! start :value start-value})
      (fn []
        {:animated a-value :done? true :value @value-atom}))))

(defn make-panresponder-data-processor [done? terminated?]
  (fn [gesture]
    {:gesture (js->clj gesture :keywordize-keys true)
     :done? done?
     :terminated? terminated?}))

(defn on-move-should-set-pan-responder? [_ gesture]
  (let [dx (.abs js/Math (oget gesture "dx"))
        dy (.abs js/Math (oget gesture "dy"))]
    (or (< 5 dx) (< 5 dy))))

(defn make-panresponder-producer [config]
  (let [panresponder-chan (chan)
        panresponder-atom (atom nil)
        active-data-processor     (make-panresponder-data-processor false false)
        done-data-processor       (make-panresponder-data-processor true false)
        terminated-data-processor (make-panresponder-data-processor true true)

        active-data-handler     #(put! panresponder-chan (active-data-processor %2))
        done-data-handler       #(put! panresponder-chan (done-data-processor %2))
        terminated-data-handler #(put! panresponder-chan (terminated-data-processor %2))

        panresponder (ocall PanResponder "create"
                            #js {:onStartShouldSetPanResponder        (constantly false)
                                 :onStartShouldSetPanResponderCapture (constantly false)
                                 :onMoveShouldSetPanResponder         on-move-should-set-pan-responder?
                                 :onMoveShouldSetPanResponderCapture  (constantly false)
                                 :onPanResponderTerminationRequest    (constantly true)
                                 :onPanResponderGrant                 active-data-handler 
                                 :onPanResponderMove                  active-data-handler 
                                 :onPanResponderRelease               done-data-handler  
                                 :onPanResponderTerminate             terminated-data-handler})]

    (put! panresponder-chan {:gesture nil :done? false :terminated? false})
    {:pan-handlers (js->clj (oget panresponder "panHandlers"))
     :producer     (fn [res-chan _]
                     (go-loop []
                       (let [value (<! panresponder-chan)]
                         (when value
                           (put! res-chan value)
                           (reset! panresponder-atom value)
                           (recur))))

                     (fn [_]
                       (let [last-panresponder @panresponder-atom]
                         (close! panresponder-chan)
                         (when (not (:done? last-panresponder))
                           (assoc last-panresponder :done? true)))))}))

(defn scroll-event-data [e]
  (let [ne (oget e :nativeEvent)
        ci (oget ne :contentInset)
        co (oget ne :contentOffset)
        cs (oget ne :contentSize)
        lm (oget ne :layoutMeasurement)]
    {:content-inset      {:top    (oget ci :top)
                          :right  (oget ci :right)
                          :bottom (oget ci :bottom)
                          :left   (oget ci :left)}
     :content-offset     {:x (oget co :x)
                          :y (oget co :y)}
     :content-size       {:height (oget cs :height)
                          :width  (oget cs :width)}
     :layout-measurement {:height (oget cs :height)
                          :width  (oget cs :width)}
     :zoom-scale         (oget e :nativeEvent.?zoomScale)}))

(defn make-scroll-producer [config]
  (let [scroll-chan (chan)
        scroll$     (atom nil)
        momentum-started?$ (atom false)]
    (put! scroll-chan [:start nil])
    {:scroll-handlers
     {:on-scroll-end-drag       #(put! scroll-chan [:scroll-end-drag (scroll-event-data %)])
      :on-momentum-scroll-begin #(reset! momentum-started?$ true)
      :on-momentum-scroll-end   #(put! scroll-chan [:momentum-scroll-end (scroll-event-data %)])
      :on-scroll                #(put! scroll-chan [:scroll (scroll-event-data %)])}
     :producer (fn [res-chan _]
                 (go-loop []
                   (let [value (<! scroll-chan)
                         [ev-name ev-value] value]
                     (when value
                       (when (= :scroll-end-drag ev-name)
                         (<! (timeout 1)))
                       (let [done? (or (= :momentum-scroll-end ev-name)
                                       (and (not @momentum-started?$)
                                            (= :scroll-end-drag ev-name)))
                             payload {:done? done? :scroll-info ev-value}]
                         (when-not (= payload @scroll$)
                           (put! res-chan payload)
                           (reset! scroll$ payload))
                         (when-not done?
                           (recur))))))
                 (fn [_]
                   (let [last-scroll @scroll$]
                     (close! scroll-chan)
                     (when (not (:done? last-scroll))
                       (assoc last-scroll :done? true)))))}))

(def native-animatable-props
  #{:opacity
    :perspective
    :rotate
    :rotate-x
    :rotate-y
    :rotate-z
    :scale
    :scale-x
    :scale-y
    :translate-x
    :translate-y
    :skew-x
    :skew-y})

(defn prepare-values [style]
  (let [prepared (helpers/prepare-values style)]
    (reduce-kv
     (fn [m k v]
       (let [native-animatable? (contains? native-animatable-props (keyword (name k)))]
         (if (not native-animatable?)
           (assoc m k (assoc v :animatable false))
           (assoc m k v))))
     {} prepared)))

(defn default-get-input-range [prop start end]
  [start end])

(defn default-get-output-range [prop start end]
  [start end])

(defn prepare-output-range [anim-values]
  (map (fn [v]
         (if (= :unit (:animatable v))
           (str (:value v) (name (:unit v)))
           (:value v)))
       anim-values))

(defn start-animation-values [config animated start-end]
  (let [from-value (get-from-value config)
        to-value (get-to-value config)]
    (reduce-kv (fn [m k v]
                 (let [{:keys [start end]} v
                       animatable          (:animatable start)]

                   (if animatable
                     (let [get-input-range    (or (:get-input-range config) default-get-input-range)
                           get-output-range   (or (:get-output-range config) default-get-output-range)
                           input-range        (apply get-input-range [k from-value to-value])
                           output-range       (apply get-output-range [k start end])
                           interpolate-config {:inputRange input-range
                                               :outputRange (prepare-output-range output-range)}]
                       (assoc m k (ocall animated "interpolate" (clj->js interpolate-config))))
                     (assoc m k (:value start)))))
               {} start-end)))

(defn get-end-range-subvec-index [value input-range]
  (if (>= 2 (count input-range))
    0
    (loop [idx 0]
      (if (= idx (- (count input-range) 2))
        idx
        (if (<= (get input-range idx)
                value
                (get input-range (inc idx)))
          idx
          (recur (inc idx)))))))

(defn end-animation-values [value config current start-end]
  (let [from-value (get-from-value config)
        to-value (get-to-value config)]
    
    (reduce-kv
     (fn [m k v]
       (let [{:keys [start end]} v
             get-input-range (or (:get-input-range config) default-get-input-range)
             get-output-range (or (:get-output-range config) default-get-output-range)
             input-range (apply get-input-range [k from-value to-value])
             output-range (apply get-output-range [k start end])

             end-range-subvec-index (get-end-range-subvec-index value input-range)

             [f-from-value f-to-value] (subvec input-range end-range-subvec-index (+ 2 end-range-subvec-index))
             [f-start f-end] (subvec output-range end-range-subvec-index (+ 2 end-range-subvec-index))
             
             animatable (:animatable start)
             start-value (or (:value f-start) (:value f-end))
             end-value (or (:value f-end) (:value f-start))]

         (if animatable
           (let [new-value
                 (cond
                   (= start-value end-value) end-value
                   (= :color animatable) (helpers/interpolate-color value start-value end-value f-from-value f-to-value)
                   (or (= :unit animatable) (= :number animatable)) (helpers/map-value-in-range value start-value end-value f-from-value f-to-value)
                   :else end-value)]
             (assoc m k (if (= :unit animatable) (str new-value (:unit end)) new-value)))
           (assoc m k (or (:value end) (:value start))))))
     current start-end)))

(defn using-native-driver? [config]
  (let [native-driver? (get-in config [:config :useNativeDriver])]
    (if (nil? native-driver?)
      true
      native-driver?)))

(defn get-start-end [prev-values next-values config]
  (let [processed-prev (merge next-values prev-values)
        processed-next (merge prev-values next-values)]
    (if (using-native-driver? config)
      (helpers/start-end-values (prepare-values processed-prev) (prepare-values processed-next))
      (let [prepared (helpers/start-end-values
                      (helpers/prepare-values processed-prev)
                      (helpers/prepare-values processed-next))]
        prepared))))

(defn render-animation-end
  ([app-db identifier] (render-animation-end app-db identifier nil nil))
  ([app-db identifier version] (render-animation-end app-db identifier version nil))
  ([app-db identifier version args]
   (let [[id _] (a/identifier->id-state identifier)
         init-meta (make-initial-meta identifier args)]

     (assoc-in app-db (a/app-db-animation-path id version)
               {:data (values init-meta)
                :meta init-meta}))))

(defn animate-state!
  ([task-runner! app-db identifier] (animate-state! task-runner! app-db identifier nil nil))
  ([task-runner! app-db identifier version] (animate-state! task-runner! app-db identifier version nil))
  ([task-runner! app-db identifier version args]
   (let [[id state] (a/identifier->id-state identifier)
         prev (a/get-animation app-db id version)
         prev-values (:data prev)
         prev-meta (:meta prev)
         init-meta (make-initial-meta identifier args prev-meta)
         config (config init-meta prev-values)
         next-values (values init-meta)
         start-end (get-start-end prev-values next-values config)
         producer (make-animated-producer config)
         task-id (a/animation-id id version)]
     (task-runner!
      producer task-id
      (fn [{:keys [value state]} app-db]
        (let [{:keys [start! done? animated value]} value]
          (if done?
            (let [current-data (:data (a/get-animation app-db id version))
                  next-data (end-animation-values
                             value 
                             config
                             current-data
                             start-end)
                  next-app-db (assoc-in app-db
                                        (a/app-db-animation-path id version)
                                        {:data next-data :meta init-meta})]
              (if (= state :keechma.toolbox.tasks/running)
                (t/stop-task next-app-db task-id)
                next-app-db))
            (let [next-data (start-animation-values config animated start-end)
                  next-app-db (assoc-in app-db
                                        (a/app-db-animation-path id version)
                                        {:data next-data :meta init-meta})]
              (start!)
              next-app-db))))))))

(defn assoc-next-pan-data [next-meta]
  (assoc next-meta :data (pan-step next-meta (:pan-value next-meta))))

(defn hex->rgb [hex]
  (let [[r g b] (helpers/hex->rgb hex)]
    (str "rgb(" r "," g "," b ")")))

(defn make-animated-values [anim-values]
  (reduce-kv (fn [m k v]
               (let [val (if (and (string? v)
                                  (str/starts-with? v "#"))
                           (hex->rgb v) 
                           v)]
                 (assoc m k (AnimatedValue. val))))
             {} anim-values))

(defn update-animated-values! [animated-values next-values]
  (reduce-kv (fn [m k v]
               (let [current (or (get m k) (AnimatedValue. v))]
                 (ocall current "setValue" v)
                 (assoc m k current)))
             animated-values next-values))

(defn rgb-props->hex [data]
  (reduce-kv
   (fn [m k v]
     (if (and (string? v) (str/starts-with? v "rgb(") (str/ends-with? v ")"))
       (let [rgb (map #(ocall js/Math :round %) (str/split (subs v 4 (dec (count v))) ","))]
         (assoc m k (helpers/rgb->hex rgb)))
       (assoc m k v)))
   {} data))

(defn panresponder-animate-state!
  ([task-runner! app-db identifier] (panresponder-animate-state! task-runner! app-db identifier nil nil))
  ([task-runner! app-db identifier version] (panresponder-animate-state! task-runner! app-db identifier version nil))
  ([task-runner! app-db identifier version args]
   (let [[id state] (a/identifier->id-state identifier)
         prev (a/get-animation app-db id version)
         prev-values (:data prev)
         prev-meta (assoc (:meta prev) :data prev-values)
         init-meta (make-initial-meta identifier args prev-meta)
         init-value (pan-init-value init-meta)
         last-values (atom (pan-step init-meta init-value))
         last-value (atom init-value)
         first-value (atom nil)
         animated-values (make-animated-values @last-values)
         {:keys [producer pan-handlers]} (make-panresponder-producer nil)
         task-id (a/animation-id id version)]
     (task-runner!
      producer task-id
      (fn [animation-state app-db]
        (let [{:keys [value state]} animation-state
              {:keys [done? terminated? gesture]} value
              init? (and (not done?) (nil? gesture))
              next-meta (assoc init-meta
                               :pan-handlers pan-handlers
                               :pan-init-value init-value
                               :pan-first-value @first-value
                               :gesture gesture
                               :data @last-values)
              next-data animated-values
              next-value (if gesture (pan-value next-meta) init-value)
              next-values (if (= next-value @last-value) @last-values (pan-step next-meta next-value))]
          
          (when (and (not= next-value @last-value))
            (reset! last-values next-values)
            (reset! last-value next-value)
            (when (and (nil? @first-value) (not= [0 0] next-value))
              (reset! first-value next-value))
            (update-animated-values! animated-values next-values))

          (if done?
            (do
              (let [next-app-db (assoc-in app-db
                                          (a/app-db-animation-path id version)
                                          {:data (rgb-props->hex next-values)
                                           :meta (assoc init-meta
                                                        :pan-handlers {}
                                                        :pan-init-value init-value
                                                        :pan-value next-value
                                                        :pan-first-value @first-value
                                                        :gesture gesture)})]
                (if (= state :keechma.toolbox.tasks/running)
                  (t/stop-task next-app-db task-id)
                  next-app-db)))
            (if init?
              (assoc-in app-db
                        (a/app-db-animation-path id version)
                        {:data next-data
                         :meta next-meta})
              app-db))))))))

(defn scroll-animate-state!
  ([task-runner! app-db identifier] (scroll-animate-state! task-runner! app-db identifier nil nil))
  ([task-runner! app-db identifier version] (scroll-animate-state! task-runner! app-db identifier version nil))
  ([task-runner! app-db identifier version args]
   (let [[id state] (a/identifier->id-state identifier)
         prev (a/get-animation app-db id version)
         prev-values (:data prev)
         prev-meta (:meta prev)
         init-meta (make-initial-meta identifier args prev-meta)
         config (scroll-config init-meta prev-values)
         next-values (values init-meta)
         start-end (get-start-end prev-values next-values config)
         {:keys [producer scroll-handlers]} (make-scroll-producer config)
         task-id (a/animation-id id version)
         animated (AnimatedValue. (or (:last-value prev-meta) 0))]
     (task-runner!
      producer task-id
      (fn [{:keys [value state times-invoked]} app-db]
        (let [{:keys [scroll-info done?]} value]
          (if-not scroll-info
            (assoc-in app-db 
                      (a/app-db-animation-path id version)
                      {:data (start-animation-values config animated start-end)
                       :meta (assoc init-meta :scroll-handlers scroll-handlers)})
            (if done?
              (do
                (let [next-app-db (assoc-in app-db
                                            (a/app-db-animation-path id version)
                                            {:data (rgb-props->hex next-values)
                                             :meta (assoc init-meta
                                                          :scroll-handlers {})})]
                  (if (= state :keechma.toolbox.tasks/running)
                    (t/stop-task next-app-db task-id)
                    next-app-db)))
              (do
                (ocall animated :setValue (scroll-value init-meta scroll-info))
                app-db))))

        )))))

(def blocking-animate-state! (partial animate-state! t/blocking-task!))
(def non-blocking-animate-state! (partial animate-state! t/non-blocking-task!))

(defn run-animation-in-group [app-db animation-config]
  (let [{:keys [animation version args]} animation-config
        a-delay (or (:delay animation-config) 0)
        runner #(blocking-animate-state! app-db animation version args)]
    (if (= 0 a-delay)
      (runner)
      (fn [ctrl app-db-atom value]
        (p/promise (fn [resolve _]
                     (js/setTimeout
                      (fn []
                        (let [r (runner)]
                          (->> (r ctrl app-db-atom value)
                               (p/map resolve)))) 
                      a-delay)))))))

(defn group-animate-state! [blocking? app-db & animations]
  (with-meta
    (fn [ctrl app-db-atom value]
      (let [done-promise
            (p/promise
             (fn [resolve _]
               (let [runners (map #(run-animation-in-group app-db %) animations)]
                 (->> (p/all (map #(% ctrl app-db-atom value) runners))
                      (p/map (fn [results]
                               (let [break? (some #(= % :keechma.toolbox.pipeline.core/break) results)]
                                 (if break?
                                   (resolve :keechma.toolbox.pipeline.core/break)
                                   (resolve)))))))))]
        (if blocking?
          done-promise
          nil)))
    {:pipeline? true}))


(def cancel-animation! a/cancel-animation!)
(def app-db-animation-path a/app-db-animation-path)
(def clear-animation a/clear-animation)
(def stop-animation! a/stop-animation!)
(def get-animation a/get-animation)
(def get-animation-state a/get-animation-state)


(def blocking-panresponder-animate-state! (partial panresponder-animate-state! t/blocking-task!))
(def non-blocking-panresponder-animate-state! (partial panresponder-animate-state! t/non-blocking-task!))

(def blocking-scroll-animate-state! (partial scroll-animate-state! t/blocking-task!))
(def non-blocking-scroll-animate-state! (partial scroll-animate-state! t/non-blocking-task!))
