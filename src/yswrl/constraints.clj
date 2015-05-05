(ns yswrl.constraints
  (:require [yswrl.db :as db]))

(def constraints
  (let [cols (db/query "SELECT table_name, column_name, character_maximum_length
                        FROM INFORMATION_SCHEMA.COLUMNS
                        WHERE table_schema = 'public'")]
    (reduce (fn [map val]
              (assoc-in map [(keyword (val :table_name))
                             (keyword (val :column_name))
                             :max-length]
                        (:character_maximum_length val)))
            {} cols)))

(defn max-length [table-key column-key]
  (get-in constraints [table-key column-key :max-length]))
