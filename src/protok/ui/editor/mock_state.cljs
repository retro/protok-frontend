(ns protok.ui.editor.mock-state)

(def state
  {:layers {1 {:id    1
               :rect  {:left   500
                       :top    300
                       :width  325
                       :height 667}
               :order 3
               :style {:background-color "#333"}}
            2 {:id    2
               :rect  {:left   900
                       :top    200
                       :width  325
                       :height 467}
               :order 2
               :style {:background-color "#af2313"}}
            3 {:id    3
               :rect  {:left   20
                       :top    20
                       :width  100
                       :height 100}
               :order 1
               :style {:background-color "#dddddd"}}}})
