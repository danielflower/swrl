(ns yswrl.links)

(defn url-encode [val] (java.net.URLEncoder/encode val "UTF-8"))

(def base-url "http://www.youshouldwatchreadlisten.com")

(defn swirl [id]
  (str base-url "/swirls/" id))

(defn user [username]
  (str base-url "/swirls/by/" (url-encode username)))