(ns source.routes.authorized)

(defn get
  {:summary "checks if authenticated"
   :responses {200 {:body [:map
                           [:user
                            [:map
                             [:id :int]
                             [:type [:enum "creator" "distributor" "admin"]]]]]}}}

  [{:keys [user] :as _request}]
  {:status 200
   :body {:user user}})
