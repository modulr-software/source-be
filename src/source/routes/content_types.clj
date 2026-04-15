(ns source.routes.content-types
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]))

(defn get
  {:summary "get all content types"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds] :as _request}]
  (-> (hon/find ds {:tname :content-types})
      (res/response)))
