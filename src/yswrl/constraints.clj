(ns yswrl.constraints
  (:require [yswrl.db :as db]))

(def cols (db/query "select table_name, column_name, character_maximum_length
from INFORMATION_SCHEMA.COLUMNS
where table_schema = 'public'"))

(defn coldef [table-name column-name]
  (first (filter #(and (= (% :table_name) (name table-name)) (= (% :column_name) (name column-name))) cols)))

(defn max-length [table-name column-name]
            (:character_maximum_length (coldef table-name column-name)))

