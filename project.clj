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

  :aliases {"fig"       ["trampoline" "run" "-m" "figwheel.main"]
            "fig:build" ["trampoline" "run" "-m" "figwheel.main" "-b" "dev" "-r"]
            "fig:min"   ["run" "-m" "figwheel.main" "-O" "advanced" "-bo" "dev"]
            "fig:test"  ["run" "-m" "figwheel.main" "-co" "test.cljs.edn" "-m" protok.test-runner]}

  :profiles {:dev {:dependencies [[com.bhauman/figwheel-main "0.1.9"]
                                  [com.bhauman/rebel-readline-cljs "0.1.4"]
                                  [com.bhauman/cljs-test-display "0.1.1"]]}})






