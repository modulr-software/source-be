(ns source.routes.report
  (:require [source.config :as conf]
            [source.email.gmail :as gmail]
            [source.email.templates :as templates]
            [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn post
  {:summary "sends us a message to let us know of a problem"
   :parameters {:body [:map [:message :string]]}
   :responses {200 {:body [:map [:message :string]]}
               401 {:body [:map [:message :string]]}
               403 {:body [:map [:message :string]]}}}
  [{:keys [ds user body] :as _req}]
  (let [{:keys [email]} (hon/find-one ds {:tname :users
                                          :where [:= :id (:id user)]})
        email-body (templates/admin-reported-problem {:user-id (:id user)
                                                      :user-type (:type user)
                                                      :user-email email
                                                      :message (:message body)})]
    (gmail/send-email {:to (conf/read-value :email :address)
                       :subject (str "Report from " email)
                       :body email-body
                       :type :text/html})
    (res/response {:message "successfully sent report"})))

(comment
  (require '[source.db.util :as db.util])

  (post {:ds (db.util/conn)
         :user {:id 3
                :type "admin"}
         :body {:message "my happy meal didn't come with a toy :("}})

  ())
