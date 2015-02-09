(defproject trainapi "0.1.0-SNAPSHOT"
  :url "http://trainapi-mamersfo.rhcloud.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.2"]
                 [compojure "1.3.1"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [yesql "0.4.0"]
                 [org.postgresql/postgresql "9.2-1004-jdbc4"]
                 [com.mchange/c3p0 "0.9.5"]
                 [com.stuartsierra/component "0.2.2"]
                 [com.taoensso/timbre "3.3.1"]
                 [org.clojure/tools.cli "0.3.1"]]
  :main trainapi.main
  :aot [trainapi.main])
