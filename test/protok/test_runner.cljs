(ns protok.test-runner
    (:require [protok.ui.editor.fsm.core-test]
              [figwheel.main.testing :refer [run-tests-async]]))

(defn -main [& args]
  (run-tests-async 5000))
