(ns source.gmail
  (:require [postal.core :as postal]
            [hiccup.page :as h]
            [source.config :as conf]))

(defn send-plaintext-email [{:keys [to subject body] :as _opts}]
  (let [email-username (conf/read-value :email :username)
        gmail-password (conf/read-value :email :password)]
    (postal/send-message
     {:host "smtp.gmail.com"
      :user email-username
      :pass gmail-password
      :port 587
      :tls true}
     {:from email-username
      :to to
      :subject subject
      :body [{:type "text/plain"
              :content body}]})))

(comment 
  (send-plaintext-email {:to "keaganncollins@gmail.com"
                         :subject "test email"
                         :body "hi this is a test coming from source-be"})

  ())
