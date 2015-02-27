(ns trainapi.utils
  (:require [taoensso.timbre :as log]))

(defn random-uuid []
  (str (java.util.UUID/randomUUID)))

(defn parse-int
  [x]
  (cond (string? x) (try (Integer/parseInt x)
                         (catch NumberFormatException e nil))
        (integer? x) (identity x)
        (float? x) (int x)
        :else nil))
