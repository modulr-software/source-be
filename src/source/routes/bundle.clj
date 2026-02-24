(ns source.routes.bundle
  (:require [ring.util.response :as res]
            [source.db.honey :as hon]
            [source.db.util :as db.util]
            [honey.sql.helpers :as hsql]
            [pg.core :as pg]))

(defn get
  {:summary "Get metadata for the associated uuid-authorized bundle."
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]}
   :responses {200 {:body [:map
                           [:id :int]
                           [:name :string]
                           [:uuid :string]
                           [:user-id :int]
                           [:video :int]
                           [:podcast :int]
                           [:blog :int]
                           [:hash [:maybe :string]]
                           [:content-type-id :int]
                           [:ts-and-cs [:maybe :int]]]}
               404 {:body [:map [:message :string]]}}}

  [{:keys [ds bundle-id] :as _request}]
  (res/response (hon/find-one ds {:tname :bundles
                                  :where [:= :id bundle-id]})))

(defn exists
  {:summary "Check for the existence of a bundle with the bundle UUID provided"
   :parameters {:query [:map [:uuid {:description "Bundle UUID"} :string]]}
   :responses {200 {:body [:map [:exists :boolean]]}
               404 {:body [:map [:message :string]]}}}
  [{:keys [ds query-params] :as _request}]
  (res/response
   (-> ds
       (pg/execute "SELECT EXISTS(SELECT 1 FROM bundles WHERE uuid = $1) AS exists" {:params [(:uuid query-params)]})
       (first))))

(comment
  (time (exists {:ds (db.util/conn)
                 :query-params {:uuid "2bbeb46bbd70c82b"}}))
  ())

