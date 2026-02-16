(ns source.routes.bundle-categories
  (:require [source.workers.bundles :as bundles]
            [ring.util.response :as res]))

(defn get
  {:summary "Get all categories for which content is present in the uuid-authorized bundle (RSS feeds / posts)."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]}
   :responses {200 {:body [:vector
                           [:map
                            [:id :int]
                            [:name :string]
                            [:display-picture {:optional true} [:maybe :string]]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [bundle-id ds] :as _request}]
  (res/response (bundles/get-bundle-categories ds bundle-id)))
