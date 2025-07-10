(ns source.routes.authorized)

(defn get [{:keys [user] :as _request}]
  {:status 200
   :body {:user user}})

(def get-responses {200 {:body [:map
                                [:user
                                 [:map
                                  [:id :int]
                                  [:type [:enum "creator" "distributor" "admin"]]]]]}})
