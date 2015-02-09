(ns trainapi.trainings
  (:refer-clojure :exclude [read])
  (:require [yesql.core :refer [defqueries]]
            [clojure.java.jdbc :as jdbc]
            [taoensso.timbre :as log]))

(defqueries "trainings.sql")

(defn create!
  [db name]
  (let [id (str (java.util.UUID/randomUUID))
        created (System/currentTimeMillis)]
    (db-insert-training! (:connection db) id name created)
    id))

(defn read
  ([db]
   (db-select-all-trainings (:connection db)))
  ([db id]
   (jdbc/with-db-transaction [conn (:connection db)]
     (if-let [training (first (db-select-training conn id))]
       (let [exercises (db-select-training-exercises conn id)]
         (assoc training :exercises (map :exercise exercises)))))))

(defn update!
  [db training]
  (jdbc/with-db-transaction [conn (:connection db)]
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
  (db-delete-training! (:connection db) id))
