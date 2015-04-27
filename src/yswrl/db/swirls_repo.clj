(ns yswrl.db.swirls-repo
  (:require [yswrl.db.core :refer [swirls]])
  )
(use 'korma.core)

(defn create-swirl [authorId title review]
  (let [swirl (insert swirls
                      (values {:author_id authorId :title title :review review}
                              ))]
    ; insert into responses
    swirl
    ))

(defn get-swirl [id]
  (first (select swirls
                 (where {:id id})
                 (limit 1))))

(defn get-swirls-authored-by [userId]
  (select swirls
          (where {:author_id userId})))
