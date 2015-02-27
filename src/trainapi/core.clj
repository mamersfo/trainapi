(ns trainapi.core
  (:gen-class)
  (:require [trainapi.handler :refer [app]]
            [immutant.web :as web]))

(defn -main [& args]
  (web/run
    app
    {:host "localhost" :port 8080}))
