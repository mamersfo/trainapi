(ns trainapi.server
  (:require [trainapi.trainings :as trainings]
            [liberator.core :refer [resource]]
            [compojure.core :refer [defroutes GET PUT ANY]]
            [compojure.route :refer [not-found]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :as json]
            [com.stuartsierra.component :as component]
            [ring.adapter.jetty :refer [run-jetty]]
            [taoensso.timbre :as log]))

(defroutes routes
  (ANY "/hello"
       []
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get]
        :handle-ok (fn [ctx] {:message "hello world!"})))

  (ANY "/trainings"
       {{name :name} :body}
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get :post]
        :post! (fn [ctx]
                 {::id (trainings/create! (::db (:request ctx)) name)})
        :post-redirect? (fn[ctx]
                          {:location (str "/trainings/" (::id ctx))})
        :handle-ok (fn [ctx]
                     (trainings/read (::db (:request ctx))))))

  (ANY "/trainings/:id"
       {{exercises :exercises} :body {id :id} :params}
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get :put :delete]
        :can-put-to-missing? false
        :exists? (fn [ctx]
                   (trainings/read (::db (:request ctx)) id))
        :put! (fn [ctx]
                (trainings/update!
                 (::db (:request ctx)) {:id id :exercises exercises}))
        :delete! (fn [ctx]
                   (trainings/delete! (::db (:request ctx)) id))
        :handle-ok (fn [ctx]
                     (trainings/read (::db (:request ctx)) id))))
  
  (not-found "404"))

(defn wrap-error-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error (class e) "-" (.getMessage e))
        (if (instance? org.postgresql.util.PSQLException e)
          (if-let [sem (.getServerErrorMessage e)]
            (do
              (println {:type :error
                        :request-uri (:uri request)
                        :request-method (:request-method request)
                        :error-type :psql
                        :error-code (.getSQLState sem)
                        :message (.getMessage sem)})
              {:status 400
               :body {:error (.getDetail sem)}})
            {:status 500
             :body {:error (.getMessage e)}})
          (do
            {:status 500
             :exception (-> e .getClass .getName)
             :message (.getMessage e)}))))))

(defn wrap-server
  [f db]
  (fn [req]
    (f (assoc req ::db db))))

(defn make-handler
  [db]
  (-> routes
      (wrap-server db)
      (wrap-params)
      (json/wrap-json-body {:keywords? true})
      (wrap-error-handling)
      (json/wrap-json-response)))

(defrecord Server [jetty port db]
  component/Lifecycle
  (start [this]
    (println ";; Starting server")
    (assoc this :jetty
           (run-jetty (make-handler db) {:port port :join? false})))
  (stop [this]
    (println ";; Stopping server")
    (if-let [jetty (:jetty this)]
      (do
        (.stop jetty)
        (dissoc this :jetty nil)))
    this))

(defn make-server
  [conf]
  (map->Server conf))
