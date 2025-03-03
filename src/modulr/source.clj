(ns modulr.source
  (:require [modulr.server :as server]
            [modulr.hooks :as hooks]))

(defn -main [& _]
  (server/start-server)
  (hooks/add-shutdown-hook server/stop-server))
