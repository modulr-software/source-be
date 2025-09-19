(ns source.routes.content-types
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get
  {:summary "get all content types"
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]]]}}}

  [{:keys [ds] :as _request}]
  (-> (services/content-types ds)
      (res/response)))
