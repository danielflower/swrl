(ns yswrl.db.swirls-repo
  (:require [yswrl.db.core :refer [swirls]])
  )
(use 'korma.core)

(defn create-swirl [authorId title review]
  (insert swirls
          (values {:author_id authorId :title title :review review }
                  )))

(defn get-swirl [id]
  (first (select swirls
                 (where {:id id})
                 (limit 1))))