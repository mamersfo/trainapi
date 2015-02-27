(ns trainapi.users
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [noir.util.crypt :as crypt]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(defn read
  ([db]
   (jdbc/query db ["select id, username, role, created from users"]))
  ([db id]
   (s/validate s/Int id)
   (first (jdbc/query db ["select * from users where id = ?" id]))))

(defn create!
  [db username password role]
  (s/validate s/Str username)
  (s/validate s/Str password)
  (s/validate s/Int role)
  (first (jdbc/insert! db :users {:username username
                                  :password (crypt/encrypt password)
                                  :created (System/currentTimeMillis)
                                  :role role})))

(defn login
  [db username password]
  (s/validate s/Str username)
  (s/validate s/Str password)
  (let [query "select * from users where username = ?"]
    (when-let [user (first (jdbc/query db [query username]))]
      (if (crypt/compare password (:password user)) user))))
