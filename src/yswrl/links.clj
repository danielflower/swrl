(ns yswrl.links)

(defn url-encode [val] (.replaceAll (java.net.URLEncoder/encode val "UTF-8") "\\+" "%20"))

(def base-url "http://www.swrl.co")

(defn absolute [relative]
  (str base-url relative))

(defn swirl [id]
  (str "/swirls/" id))

(defn notifications []
  "/notifications")

(defn edit-swirl
  ([id origin-swirl-id]
   (if (nil? origin-swirl-id)
     (str "/swirls/" id "/edit")
     (str "/swirls/" id "/edit?origin-swirl-id=" origin-swirl-id)))
  ([id]
   (edit-swirl id nil)))

(defn delete-swirl [id]
  (str "/swirls/" id "/delete"))

(defn user [username]
  (str "/swirls/by/" (url-encode username)))

(defn password-reset [token]
  (str "/reset-password?token=" (url-encode token)))

(defn itunes-album [album-id]
  (str "https://itunes.apple.com/us/album/id" album-id "?at="))

(defn gravatar-url [hash size]
  (str "http://www.gravatar.com/avatar/" hash "?s=" size "&d=monsterid"))

(defn create-swirl []
  "/swirls/start")

(defn inbox
  ([] "/swirls/inbox")
  ([response]
   (str "/swirls/inbox/" (.toLowerCase (url-encode response)))))
