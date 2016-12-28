(defproject cloko "0.0.1"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293"]]
  :plugins [[lein-figwheel "0.5.8"]]
  :profiles {:test {:dependencies [[lein-kibit "0.1.3"]]}}
  :clean-targets [:target-path "out"]
  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main "cloko.frontend"}}]})


