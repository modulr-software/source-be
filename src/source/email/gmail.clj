(ns source.email.gmail
  (:require [postal.core :as postal]
            [source.config :as conf]))

(defn postal-config []
  (let [email-username (conf/read-value :email :username)
        gmail-password (conf/read-value :email :password)]
    {:host "smtp.gmail.com"
     :user email-username
     :pass gmail-password
     :port 587
     :tls true}))

(defn send-plaintext-email [{:keys [to subject body] :as _opts}]
  (let [email-username (conf/read-value :email :username)]
    (postal/send-message
     (postal-config)
     {:from email-username
      :to to
      :subject subject
      :body [{:type "text/plain"
              :content body}]})))

(defn send-html-email [{:keys [to subject body] :as _opts}]
  (let [email-username (conf/read-value :email :username)]
    (postal/send-message
     (postal-config)
     {:from email-username
      :to to
      :subject subject
      :body [{:type "text/html"
              :content body}]})))

(comment
  (require '[source.email.templates :as templates])

  (send-plaintext-email {:to "keaganncollins@gmail.com"
                         :subject "test email"
                         :body "hi this is a test coming from source-be"})

  (send-html-email {:to "keaganncollins@gmail.com"
                    :subject "feed rejection template"
                    :body (templates/feed-rejection {:creator-name "Keagan"
                                                     :feed-title "Keagan's Mukbang Channel"
                                                     :reason "too cringe frfr"})})

  (send-html-email {:to "keaganncollins@gmail.com"
                    :subject "feed approval template"
                    :body (templates/feed-approval {:creator-name "Keagan"
                                                    :feed-title "Keagan's Mukbang Channel"
                                                    :feed-id 2})})

  (send-html-email {:to "keaganncollins@gmail.com"
                    :subject "admin reported problem"
                    :body (templates/admin-reported-problem {:user-id 3
                                                             :user-email "keaganncollins@gmail.com"
                                                             :user-type "creator"
                                                             :message "no burger king foot lettuce :("})})

  ())
