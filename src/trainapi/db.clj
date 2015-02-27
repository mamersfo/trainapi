(ns trainapi.db
  (:require [clj-dbcp.core :as dbcp]))

(defn make-subname
  [host port db]
  (str "//" host ":" port "/" db))

(def host (or (System/getenv "OPENSHIFT_POSTGRESQL_DB_HOST")     "localhost"))
(def port (or (System/getenv "OPENSHIFT_POSTGRESQL_DB_PORT")     "5432"))
(def db   (or (System/getenv "OPENSHIFT_APP_NAME")               "tomcat"))
(def user (or (System/getenv "OPENSHIFT_POSTGRESQL_DB_USERNAME") "postgres"))
(def pass (or (System/getenv "OPENSHIFT_POSTGRESQL_DB_PASSWORD") "postgres"))

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
