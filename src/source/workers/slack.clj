(ns source.workers.slack
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as http]
            [source.config :as conf]
            [source.util :as util]
            [source.db.honey :as hon]
            [source.services.analytics.interface :as analytics]))

(defn send-slack-message! [{:keys [channel-id message blocks unfurl? access-token]}]
  @(http/request {:url "https://slack.com/api/chat.postMessage"
                  :method :post
                  :headers {"Authorization" (str "Bearer " (or access-token (conf/read-value :slack :token)))
                            "Content-Type" "application/json; charset=utf-8"}
                  :body (json/write-str {:channel channel-id
                                         :text message
                                         :blocks blocks
                                         :unfurl_links unfurl?
                                         :unfurl_media unfurl?})}))

(defn slack-post! [ds post bundle-id channel-id]
  (let [section (cond
                  (= (:content-type-id post) 1)
                  (str ":clapper: *" (:feed-title post) " — " (:title post) "*\n")
                  (= (:content-type-id post) 2)
                  (str ":studio_microphone: *" (:feed-title post) " — " (:title post) "*\n")
                  (= (:content-type-id post) 3)
                  (str ":newspaper: *" (:feed-title post) " — " (:title post) "*\n"))

        verb (cond
               (= (:content-type-id post) 1)
               "Watch"
               (= (:content-type-id post) 2)
               "Listen"
               (= (:content-type-id post) 3)
               "Read")

        message (cond
                  (= (:content-type-id post) 1)
                  (str section
                       (:stream-url post))
                  (= (:content-type-id post) 2)
                  (str section
                       (util/clean (:info post)) "\n"
                       (or (:url post)
                           (:stream-url post)))
                  (= (:content-type-id post) 3)
                  (str section
                       (util/clean (:info post)) "\n"
                       (or (:url post)
                           (:stream-url post))))

        blocks [{:type "section"
                 :text {:type "mrkdwn"
                        :text section}
                 :accessory {:type "image"
                             :image_url (:thumbnail post)
                             :alt_text (:title post)}}
                {:type "actions"
                 :elements [{:type "button"
                             :text {:type "plain_text"
                                    :text verb}
                             :url (or (:url post) (:stream-url post))}]}]

        {:keys [access-token]} (hon/find-one ds {:tname :integration-channels
                                                 :where [:and
                                                         [:= :channel-id channel-id]
                                                         [:= :bundle-id bundle-id]]})]

    (if (util/unfurlable? (or (:url post) (:stream-url post)))
      (send-slack-message! {:channel-id channel-id
                            :message message
                            :access-token access-token
                            :unfurl? true})
      (send-slack-message! {:channel-id channel-id
                            :message message
                            :access-token access-token
                            :blocks blocks
                            :unfurl? false}))

    (analytics/insert-bot-post! ds post bundle-id)))

(comment
  #_(slack-post! (db.util/conn) 26 "C0AV8471CJE")
  ())
