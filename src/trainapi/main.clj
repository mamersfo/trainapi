(ns trainapi.main
  (:gen-class)
  (:require [trainapi.db :as db]
            [trainapi.server :as server]
            [com.stuartsierra.component :as component]
            [taoensso.timbre :as log]
            [clojure.tools.cli :refer [cli]]))

(log/set-config! [:appenders :spit :enabled?] true)
(log/set-config! [:shared-appender-config :spit-filename] "server.log")

(defn make-subname
  [host port db]
  (str "//" host ":" port "/" db))

(def config
  {:openshift {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"
               :subname (make-subname
                         (System/getenv "OPENSHIFT_POSTGRESQL_DB_HOST")
                         (System/getenv "OPENSHIFT_POSTGRESQL_DB_PORT")
                         (System/getenv "OPENSHIFT_APP_NAME"))
               :user (System/getenv "OPENSHIFT_POSTGRESQL_DB_USERNAME")
               :password (System/getenv "OPENSHIFT_POSTGRESQL_DB_USERNAME")
               :port 80}
   :local     {:classname "org.postgresql.Driver"
               :subprotocol "postgresql"
               :subname (make-subname "localhost" "5432" "tomcat")
               :user "postgres"
               :password "postgres"
               :port 8080}})

(defn make-system
  [conf]
  (component/system-map
   :db (db/make-database conf)
   :server (component/using
            (server/make-server conf)
            {:db :db})))

(defn -main [& args]
  (let [[opts _ _] (cli args
                        ["-l" "--local" "Run local" :default false :flag true])
        conf (if (:local opts) (:local config) (:openshift config))]
    (component/start (make-system conf))))
