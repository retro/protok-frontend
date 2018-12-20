(defproject sf "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339"]
                 [reagent "0.8.1"]
                 [keechma "0.3.13" :exclusions [cljsjs/react-with-addons cljsjs/react-dom cljsjs/react-dom-server]]
                 [keechma/toolbox "0.1.20" :exclusions [cljsjs/react-with-addons cljsjs/react-dom cljsjs/react-dom-server]]
                 [org.clojars.mihaelkonjevic/garden-basscss "0.2.2"]
                 [binaryage/oops "0.6.2"]
                 [medley "1.0.0"]
                 [com.rpl/specter "1.1.1"]
                 [clj-tagsoup "0.3.0" :exclusions [org.clojure/clojure org.clojure/data.xml]]
                 [org.clojure/data.xml "0.0.8"]
                 [floatingpointio/graphql-builder "0.1.8"]
                 [hodgepodge "0.1.3"]]


  :source-paths ["src" "test"]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :clean-targets ^{:protect false} ["resources/public/js"
                                    "target"
                                    "test/js"]

  :figwheel {:css-dirs ["resources/public/css"]}


  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[figwheel-sidecar "0.5.10"]
                   [com.cemerick/piggieback "0.2.1"]
                   [binaryage/devtools "0.8.2"]]

    :plugins      [[lein-figwheel "0.5.16"]
                   [lein-doo "0.1.7"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src"]
     :figwheel     {:on-jsload "protok.core/reload"}
     :compiler     {:main                 protok.core
                    :optimizations        :none
                    :output-to            "resources/public/js/app.js"
                    :output-dir           "resources/public/js/dev"
                    :asset-path           "js/dev"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config
                    {:devtools/config
                     {:features-to-install    [:formatters :hints]
                      :fn-symbol              "F"
                      :print-config-overrides true}}}}

    {:id           "min"
     :source-paths ["src"]
     :compiler     {:main            protok.core
                    :optimizations   :advanced
                    :output-to       "resources/public/js/app.js"
                    :output-dir      "resources/public/js/min"
                    :elide-asserts   true
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}

    {:id           "test"
     :source-paths ["src" "test"]
     :compiler     {:output-to     "resources/public/js/test.js"
                    :output-dir    "resources/public/js/test"
                    :main          protok.runner
                    :optimizations :none}}]})






