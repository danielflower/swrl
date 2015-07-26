(ns yswrl.user.user-selector)

(defn usernames-and-emails-from-request [checkboxes-raw textbox-raw]
  (let [textbox (if (clojure.string/blank? textbox-raw)
                  []
                  (map #(.trim %) (clojure.string/split textbox-raw #"[,;]")))
        checkboxes (if (vector? checkboxes-raw)
                     checkboxes-raw
                     (if (clojure.string/blank? checkboxes-raw)
                       []
                       [(.trim checkboxes-raw)]))
        ]
    (distinct (concat checkboxes textbox))))

