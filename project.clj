(defproject trainapi "0.1.0-SNAPSHOT"
  :url "http://trainapi-mamersfo.rhcloud.com"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [liberator "0.12.2"]
                 [compojure "1.3.2"]
                 [ring/ring-core "1.3.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [org.postgresql/postgresql "9.2-1004-jdbc4"]
                 [com.stuartsierra/component "0.2.2"]
                 [com.taoensso/timbre "3.4.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.immutant/immutant "2.0.0-beta2"
                  :exclusions [[ch.qos.logback/logback-classic]]]
                 [lib-noir "0.9.5"]
                 [prismatic/schema "0.3.7"]
                 [clj-dbcp "0.8.1"]
                 [org.slf4j/slf4j-log4j12 "1.7.10"]]

  :repositories [["Immutant incremental builds"
                  "http://downloads.immutant.org/incremental/"]]

  :plugins [[lein-immutant "2.0.0-beta1"]
            [lein-ring "0.9.1"]]

  :ring {:handler trainapi.handler/app
         :nrepl {:start? true :port 4555}})
