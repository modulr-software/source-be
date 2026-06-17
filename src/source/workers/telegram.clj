(ns source.workers.telegram
  (:require [org.httpkit.client :as http]
            [source.config :as conf]
            [clojure.data.json :as json]
            [source.workers.bundles :as bundles]
            [source.util :as util]
            [source.db.util :as db.util]))

(defn send-telegram-photo! [chat-id thumbnail caption reply-markup]
  @(http/request {:url (str "https://api.telegram.org/bot" (conf/read-value :telegram :token) "/sendPhoto")
                  :method :post
                  :headers {"Content-Type" "application/json; charset=utf-8"}
                  :body (json/write-str {:chat_id chat-id
                                         :photo thumbnail
                                         :caption caption
                                         :parse_mode "HTML"
                                         :reply_markup reply-markup})}))

(defn send-telegram-message! [chat-id message web-preview?]
  @(http/request {:url (str "https://api.telegram.org/bot" (conf/read-value :telegram :token) "/sendMessage")
                  :method :post
                  :headers {"Content-Type" "application/json; charset=utf-8"}
                  :body (json/write-str {:chat_id chat-id
                                         :text message
                                         :parse_mode "HTML"
                                         :disable_web_page_preview web-preview?})}))

(defn telegram-post! [post channel-id]
  (let [section (cond
                  (= (:content-type-id post) 1)
                  (str "🎬 <b>" (:feed-title post) " — " (:title post) "</b>\n\n")
                  (= (:content-type-id post) 2)
                  (str "🎙 <b>" (:feed-title post) " — " (:title post) "</b>\n\n")
                  (= (:content-type-id post) 3)
                  (str "📰 <b>" (:feed-title post) " — " (:title post) "</b>\n\n"))

        verb (cond
               (= (:content-type-id post) 1)
               "Watch"
               (= (:content-type-id post) 2)
               "Listen"
               (= (:content-type-id post) 3)
               "Read")

        reply-markup (when (or (:url post) (:stream-url post))
                       {:inline_keyboard [[{:text verb
                                            :url
                                            (or (:url post) (:stream-url post))}]]})

        message (cond
                  (= (:content-type-id post) 1)
                  (str section
                       (:stream-url post))
                  (= (:content-type-id post) 2)
                  (str section
                       (or (:url post)
                           (:stream-url post)))
                  (= (:content-type-id post) 3)
                  (str section
                       (or (:url post)
                           (:stream-url post))))]

    (if (util/unfurlable? (or (:url post) (:stream-url post)))
      (send-telegram-message! channel-id message false)
      (send-telegram-photo!
       channel-id
       (:thumbnail post)
       (str section (util/truncate (util/strip-tags (or (:info post) " ")) 600) "\n")
       reply-markup))))

(comment
  #_(telegram-post! (db.util/conn) 26 "-5073615757")
  ())
