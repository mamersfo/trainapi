(ns trainapi.db
  (:require [com.stuartsierra.component :as component])
  (:import com.mchange.v2.c3p0.ComboPooledDataSource))

(defrecord Database [classname subprotocol subname user password connection]
  component/Lifecycle
  (start [this]
    (println ";; Starting database")
    (let [url (str "jdbc:" subprotocol ":" subname)
          ds (doto (ComboPooledDataSource.)
               (.setDriverClass classname)
               (.setJdbcUrl url)
               (.setUser user)
               (.setPassword password)
               (.setMaxIdleTimeExcessConnections (* 30 60))
               (.setMaxIdleTime (* 3 60 60)))]
      (assoc this :connection {:datasource ds})))
  (stop [this]
    (println ";; Stopping database")
    (if-let [conn (:connection this)]
      (.close (:datasource conn)))
    this))

(defn make-database
  [conf]
  (map->Database conf))
