(ns yswrl.db.swirls-repo
  (:require [yswrl.db.core :refer [swirls]])
  )
(use 'korma.core)
(use 'korma.db)

(defn create-swirl [authorId title review recipientNames]
  (let [questions (->> (repeat (count recipientNames) "?")
                       (interpose ",")
                       (apply str))]
    (transaction
      (let [swirl (insert swirls
                          (values {:author_id authorId :title title :review review}))
            ]
        (exec-raw [
                   (str "INSERT INTO swirl_responses (swirl_id, responder) "
                        "SELECT " (:id swirl) ", id FROM users WHERE username IN ( " questions " )") recipientNames])
        swirl))))

(defn get-swirl [id]
  (first (select swirls
                 (where {:id id})
                 (limit 1))))

(defn get-swirls-authored-by [userId]
  (select swirls
          (where {:author_id userId})))
