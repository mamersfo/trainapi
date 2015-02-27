(ns trainapi.core
  (:gen-class)
  (:require [trainapi.handler :refer [app]]
            [immutant.web :as web]
            [environ.core :refer (env)]))

(defn -main [& {:as args}]
  (web/run
    app
    (merge {"host" (env :trainapi-web-host)
            "port" (env :trainapi-web-port)}
           args)))
