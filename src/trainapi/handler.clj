(ns trainapi.handler
  (:require [trainapi.trainings :as trainings]
            [trainapi.users :as users]
            [trainapi.utils :refer [parse-int]]
            [trainapi.db :refer [db server-error-message]]
            [liberator.core :refer [resource]]
            [liberator.representation :refer [ring-response as-response]]
            [compojure.core :refer [defroutes GET POST PUT ANY]]
            [compojure.route :refer [not-found]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :as json]
            [immutant.web.middleware :as immutant]
            [taoensso.timbre :as log]))

(defn authorized?
  [ctx]
  (if-let [request (:request ctx)]
    (if-let [user (-> request :session :user)]
      (or (= 2 (:role user))
          (= (:id user) (-> request :route-params :user-id)))
      (println "no user"))))

(defn admin?
  [ctx]
  (if-let [user (-> ctx :request :session :user)]
    (= 2 (:role user))
    (println "no user")))

(defroutes routes
  (ANY "/hello"
       []
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get]
        :handle-ok (fn [ctx] {:message "hello world!"})))

  (ANY "/login"
       {{username :username password :password} :body}
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:post]
        :post! (fn [ctx]
                 (when-let [user (users/login db username password)]
                   {::user (dissoc user :password)}))
        :new? false
        :respond-with-entity? true
        :handle-ok (fn [ctx] (::user ctx))
        :as-response (fn [d ctx]
                       (-> (as-response d ctx)
                           (assoc-in [:session :user] (::user ctx))))))

  (ANY "/users"
       []
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get]
        :authorized? admin?
        :handle-ok (fn [ctx] (users/read db))))
  
  (ANY "/users/:user-id"
       [user-id]
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get]
        :exists? (fn [ctx]
                   (if-let [user (users/read db (parse-int user-id))]
                     {::user (dissoc user :password)}))
        :authorized? authorized?
        :handle-ok (fn [ctx] (::user ctx))))
  
  (ANY "/users/:user-id/trainings"
       {{user-id :user-id} :params {name :name} :body}
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get :post]
        :authorized? authorized?
        :post! (fn [ctx]
                 {::training (trainings/create! db (parse-int user-id) name)})
        :new? true
        :post-redirect? (fn[ctx]
                          (let [id (:id (::training ctx))
                                uri (str "/users/" user-id "/trainings/" id)]
                            {:location uri}))
        :handle-ok (fn [ctx] (trainings/read-for-user db (parse-int user-id)))))

  (ANY "/users/:user-id/trainings/:training-id"
       {{exercises :exercises} :body
        {user-id :user-id training-id :training-id} :params}
       (resource
        :available-media-types ["application/json"]
        :allowed-methods [:get :put :delete]
        :authorized? authorized?
        :can-put-to-missing? false
        :exists? (fn [ctx]
                   (if-let [found (trainings/read db (parse-int training-id))]
                     {::training found}))
        :put! (fn [ctx]
                (trainings/update! db {:id (parse-int training-id)
                                       :exercises exercises}))
        :delete! (fn [ctx] (trainings/delete! db (parse-int training-id)))
        :handle-ok (fn [ctx] (::training ctx))))
  
  (not-found "404"))

(defn wrap-error-handling
  [handler]
  (fn [request]
    (try
      (handler request)
      (catch Exception e
        (log/error (class e) "-" (.getMessage e))
        (if-let [msg (server-error-message e)]
          {:status 400
           :body {:message msg}
           :session (:session request)}
          {:status 500
           :body {:message (.getMessage e)}
           :session (:session request)})))))

(def app
  (-> routes
      (wrap-params)
      (json/wrap-json-body {:keywords? true})
      (wrap-error-handling)
      (json/wrap-json-response)
      (immutant/wrap-session {:timeout 10})))
