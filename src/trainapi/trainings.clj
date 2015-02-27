(ns trainapi.trainings
  (:refer-clojure :exclude [read])
  (:require [clojure.java.jdbc :as jdbc]
            [schema.core :as s]
            [taoensso.timbre :as log]))

(def Training
  {(s/required-key :id) s/Int
   (s/required-key :exercises) [s/Str]})

(defn create!
  [db user-id name]
  (s/validate s/Int user-id)
  (s/validate s/Str name)
  (let [created (System/currentTimeMillis)]
    (first
     (jdbc/insert! db :trainings
                   {:user_id user-id :name name :created created}))))

(defn read
  [db id]
  (s/validate s/Int id)
  (jdbc/with-db-transaction [conn db]
    (if-let [training
             (first (jdbc/query db ["select * from trainings where id = ?" id]))]
      (let [exercises
            (jdbc/query db ["select exercise from trainingexercises
                              where training = ? order by position asc" id])]
        (assoc training :exercises (map :exercise exercises))))))

(defn read-for-user
  [db user-id]
  (s/validate s/Int user-id)
  (jdbc/query db ["select * from trainings where user_id = ?" user-id]))

(defn update!
  [db training]
  (s/validate Training training)
  (jdbc/with-db-transaction [conn db]
    (jdbc/delete! conn :trainingexercises ["training = ?" (:id training)])
    (let [exercises (map (fn [exercise position]
                         {:training (:id training)
                          :exercise exercise
                          :position position})
                       (:exercises training)
                       (range (count (:exercises training))))]
      (apply (partial jdbc/insert! conn :trainingexercises) exercises))))

(defn delete!
  [db id]
  (s/validate s/Int id)
  (jdbc/delete! db :trainings ["id = ?" id]))
