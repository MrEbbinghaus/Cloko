(defproject cloko "0.0.1"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]
                 [org.clojure/test.check "0.9.0"]
                 [devcards "0.2.2"]
                 [lein-doo "0.1.7"]]
  :plugins [[lein-figwheel "0.5.8"]
            [lein-doo "0.1.7"]
            [devcards "0.2.2"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]
  :hooks [leiningen.cljsbuild]
  :profiles {:test {:plugins [[lein-kibit "0.1.3"]]}}
  :clean-targets [:target-path "out"]
  :cljsbuild {
              :test-commands {"test" ["lein" "doo" "phantom" "test" "once"]}
              :builds {:dev {:source-paths ["src"]
                             :figwheel true
                             :compiler {:main "cloko.frontend"
                                        :output-dir "resources/public/cljs/src/out"}}
                       :test {
                              :source-paths ["src" "test"]
                              :compiler {:main runners.doo
                                         :optimizations :none
                                         :output-dir "resources/public/cljs/tests/out"
                                         :output-to "resources/public/cljs/tests/all-tests.js"}}
                       :devcard-test {
                                      :source-paths ["src" "test"]
                                      :figwheel {:devcards true}
                                      :compiler {:main runners.browser
                                                 :optimizations :none
                                                 :asset-path "cljs/devcard-tests/out"
                                                 :output-to "resources/public/cljs/devcard-tests/all-tests.js"
                                                 :output-dir "resources/public/cljs/devcard-tests/out"
                                                 :source-map-timestamp true}}}})
