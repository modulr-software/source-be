(ns modulr.source
  (:require [modulr.server :as server]
            [modulr.hooks :as hooks]
            [clj-reload.core :as reload]
            ))

(defn after-ns-reload []
  (server/restart-server))

(comment
  (server/start-server)
  (server/stop-server)
  (server/running?)
  (server/restart-server))

(defn -main [& _]
  (server/start-server)
  (hooks/add-shutdown-hook server/stop-server))
