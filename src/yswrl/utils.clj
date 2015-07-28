(ns yswrl.utils)

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn user-from-session
  [req]
  (:user (:session req)))
