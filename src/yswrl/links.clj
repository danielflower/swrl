(ns yswrl.links)

(defn url-encode [val] (java.net.URLEncoder/encode val "UTF-8"))

(def base-url "http://www.youshouldwatchreadlisten.com")

(defn absolute [relative]
  (str base-url relative))

(defn swirl [id]
  (str "/swirls/" id))

(defn edit-swirl [id]
  (str "/swirls/" id "/edit"))

(defn user [username]
  (str "/swirls/by/" (url-encode username)))

(defn password-reset [token]
  (str "/reset-password?token=" (url-encode token)))