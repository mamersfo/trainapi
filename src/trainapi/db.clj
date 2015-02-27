(ns trainapi.db
  (:require [clj-dbcp.core :as dbcp]
            [environ.core :refer [env]]))

(defn make-subname
  [host port db]
  (str "//" host ":" port "/" db))

(def host (or (env :openshift-postgresql-db-host)     "localhost"))
(def port (or (env :openshift-postgres-db-port)       "5432"))
(def db   (or (env :openshift-app-name)               "tomcat"))
(def user (or (env :openshift-postgresql-db-host)     "postgres"))
(def pass (or (env :openshift-postgresql-db-password) "postgres"))

(def db-spec {:subprotocol "postgresql"
              :subname (make-subname host port db)
              :user user
              :password pass})

(def db {:datasource (dbcp/make-datasource {:adapter :postgresql
                                            :host host
                                            :database db
                                            :username user
                                            :password pass})})

(defn server-error-message
  [e]
  (if (instance? org.postgresql.util.PSQLException e)
    (if-let [msg (.getServerErrorMessage e)]
      (.getDetail msg)
      (.getMessage e))))
