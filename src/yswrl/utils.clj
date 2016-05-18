(ns yswrl.utils)

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn user-from-session
  [req]
  (:user (:session req)))

(defn interleave-differing-lengths
  [& seq-seq]
  (when seq-seq
    (lazy-seq
      (concat (filter identity
                      (map first seq-seq))
              (apply interleave-differing-lengths
                     (seq
                       (filter identity
                               (map next seq-seq))))))))
