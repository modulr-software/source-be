(ns source.routes.bundle-categories
  (:require [source.services.interface :as services]
            [source.db.util :as db.util]
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
  (with-open [bundle-ds (db.util/conn :bundle bundle-id)]
    (let [feed-ids (->> (services/outgoing-posts bundle-ds)
                        (mapv :feed-id))
          category-ids (->> (services/feed-categories ds {:where [:in :feed-id feed-ids]})
                            (mapv :category-id))]
      (res/response (services/categories ds {:where [:in :id category-ids]})))))
