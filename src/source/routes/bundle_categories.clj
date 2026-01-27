(ns source.routes.bundle-categories
  (:require [source.workers.bundles :as bundles]
            [ring.util.response :as res]))

(defn get
  {:summary "get categories in the uuid-authorized bundle"
   :parameters {:query [:map [:uuid :string]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [bundle-id ds] :as _request}]
  (res/response (bundles/get-bundle-categories ds bundle-id)))
