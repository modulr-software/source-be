(ns source.workers.slack
  (:require [clojure.data.json :as json]
            [source.workers.bundles :as bundles]
            [org.httpkit.client :as http]
            [source.config :as conf]
            [clojure.string :as str]
            [source.util :as util]
            [source.db.util :as db.util]))

(defn strip-html [s]
  (-> s
      (str/replace #"<br\s*\/?>" "\n")
      (str/replace #"<\/p>" "\n\n")
      (str/replace #"<[^>]*>" "")))

(defn truncate [s max-len]
  (if (> (count s) max-len)
    (subs s 0 max-len)
    s))

(defn clean [s]
  (-> s
      (strip-html)
      (truncate 600)))

(defn send-slack-message! [channel-id message blocks]
  @(http/request {:url "https://slack.com/api/chat.postMessage"
                  :method :post
                  :headers {"Authorization" (str "Bearer " (conf/read-value :slack :token))
                            "Content-Type" "application/json; charset=utf-8"}
                  :body (json/write-str {:channel channel-id
                                         :text message
                                         :blocks blocks})}))

(defn slack-post! [ds bundle-id channel-id]
  (let [post (-> (bundles/get-outgoing-posts
                  ds
                  {:bundle-id bundle-id
                   :start 0
                   :limit 1
                   :type 3
                   :seed (util/uuid)
                   :truncate false})
                 (:data)
                 (first))

        verb (cond
               (= (:content-type-id post) 1) "Watch"
               (= (:content-type-id post) 2) "Listen"
               (= (:content-type-id post) 3) "Read")

        section (cond
                  (= (:content-type-id post) 1)
                  (str ":clapper: *" (:feed-title post) " — " (:title post) "*\n")
                  (= (:content-type-id post) 2)
                  (str ":studio_microphone: *" (:feed-title post) " — " (:title post) "*\n")
                  (= (:content-type-id post) 3)
                  (str ":newspaper: *" (:feed-title post) " — " (:title post) "*\n"))

        message (cond
                  (= (:content-type-id post) 1)
                  (str section
                       (:stream-url post))
                  (= (:content-type-id post) 2)
                  (str section
                       (clean (:info post)) "\n"
                       (:stream-url post))
                  (= (:content-type-id post) 3)
                  (str section
                       (clean (:info post)) "\n"
                       (or (:url post)
                           (:stream-url post)
                           (:thumbnail post))))

        blocks [{:type "section"
                 :text {:type "mrkdwn"
                        :text (str section (clean (:info post)))}
                 :accessory {:type "image"
                             :image_url (:thumbnail post)
                             :alt_text (:title post)}}
                {:type "actions"
                 :elements [{:type "button"
                             :text {:type "plain_text"
                                    :text verb}
                             :url (or (:url post)
                                      (:stream-url post))}]}]]
    (send-slack-message! channel-id message blocks)))

(comment
  (slack-post! (db.util/conn) 26 "C0AV8471CJE")
  ())
