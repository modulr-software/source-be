(ns source.routes.job
  (:require [source.services.interface :as services]
            [ring.util.response :as res]
            [source.routes.openapi :as api]
            [source.workers.schemas :as schemas]))

(defn get
  {:summary "View a single job's raw metadata by id"
   :parameters (api/params :path [:map [:id :int]])
   :responses (api/success schemas/JobWithMetadata)}

  [{:keys [ds path-params] :as _req}]
  (let [job (services/job ds path-params)
        metadata (services/job-metadata ds {:id (:job-metadata-id job)})]
    (if (some? job)
      (res/response (merge (dissoc job :job-metadata-id)
                           {:metadata metadata}))
      (res/response {}))))

(comment
  (require '[source.db.util :as db.util])
  (def ds (db.util/conn))

  (get {:ds ds :path-params {:id 5}})

  ())
