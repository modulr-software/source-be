(ns source.routes.job
  (:require [source.services.interface :as services]
            [ring.util.response :as res]))

(defn get [{:keys [ds path-params] :as _req}]
  (let [job (services/job ds path-params)
        metadata (services/job-metadata ds {:id (:job-metadata-id job)})]
    (res/response (merge (dissoc job :job-metadata-id)
                         {:metadata metadata}))))

(comment
  (require '[source.db.util :as db.util])
  (def ds (db.util/conn))

  (get {:ds ds :path-params {:id 1}})

  ())
