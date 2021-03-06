(ns yswrl.links)

(defn url-encode [val] (.replaceAll (java.net.URLEncoder/encode (or val "") "UTF-8") "\\+" "%20"))

(def base-url "https://www.swrl.co")

(defn absolute [relative]
  (str base-url relative))

(defn swirl
  ([id comment-id]
   (if (nil? comment-id)
     (str "/swirls/" id)
     (str "/swirls/" id "#" comment-id)))
  ([id]
   (swirl id nil)))

(defn notifications []
  "/notifications")

(defn edit-swirl
  ([id query-string]
   (if (nil? query-string)
     (str "/swirls/" id "/edit")
     (str "/swirls/" id "/edit?" query-string)))
  ([id]
   (edit-swirl id nil)))

(defn delete-swirl [id]
  (str "/swirls/" id "/delete"))

(defn user [username]
  (str "/profile/" (url-encode username)))

(defn password-reset [token]
  (str "/reset-password?token=" (url-encode token)))

(defn itunes-album [album-id]
  (str "https://itunes.apple.com/us/album/id" album-id "?at=1001l55M"))

(defn gravatar-url [hash size]
  (str "https://www.gravatar.com/avatar/" hash "?s=" size "&d=monsterid&r=pg"))

(defn facebook-image-url [facebook-id size]
  (str "https://graph.facebook.com/" facebook-id "/picture?width=" size "&height=" size))

(defn create-swirl []
  "/swirls/start")

(defn notification-options [email]
  (str "/notification-options?email=" (url-encode email)))

(defn inbox
  ([] "/swirls/inbox")
  ([response]
   (str "/swirls/inbox/" (.toLowerCase (url-encode response)))))

(defn join-group
  ([group-id join-code]
   (str "/groups/" group-id "/join/" join-code))
  ([group]
   (join-group (group :id) (group :join_code))))

(defn group [group-id]
  (str "/groups/" group-id))
(defn edit-group [group-id]
  (str "/groups/" group-id "/edit"))

