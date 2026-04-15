(ns source.server
  (:require [org.httpkit.server :as http]
            [source.db.interface :as db]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.routes.interface :as routes]
            [source.util :as util]
            [taoensso.telemere :as t]
            [source.config :as conf]))

(defonce ^:private *components (atom nil))

(defn initialise-server! [{:keys [ds js]}]
  (http/run-server
   (routes/create-app {:ds ds
                       :js js})
   {:port (conf/read-value :port)}))

(defn initialise-job-service! [{:keys [ds] :as _deps}]
  (->> (jobs/interrupted-jobs ds)
       (congest/create-job-service)))

(defn component-on? [component]
  (if (some? (get @*components component))
    true
    false))

(defn deps-on? [deps]
  (every? component-on? deps))

(defn initialise!
  "executes the init-fn on the provided component and, if successful, updates the components atom with the new component"
  [{:keys [name init-fn deps] :as _component}]
  (try
    (when (deps-on? deps)
      (swap! *components assoc name (init-fn @*components)))
    (catch Exception e (t/log! {:level :error
                                :msg (str "Failed to initialise " name ": " e)}))))

(defn initialise-components! [components]
  (run! initialise! components))

(defn running? []
  (some? (:server @*components)))

(defn start-server []
  (cond (not (some? (:server @*components)))
        (do
          (t/log! (str "Starting server on port " (conf/read-value :port) "..."))
          (initialise-components! [{:name :ds
                                    :init-fn (fn [_deps] (db/ds :master))
                                    :deps []}
                                   {:name :js
                                    :deps [:ds]
                                    :init-fn initialise-job-service!}
                                   {:name :server
                                    :deps [:ds :js]
                                    :init-fn initialise-server!}]))
        :else
        (t/log! "Server already running!")))

(defn stop-server []
  (t/log! "Stopping server...")
  (when (some? (:js @*components))
    (congest/kill! (:js @*components)))
  (when (some? (:server @*components))
    (let [server-stop (:server @*components)]
      (server-stop)))
  (reset! *components nil))

(defn restart-server [& {:keys [keep-js]}]
  (if keep-js
    (do
      (when (some? (:server @*components))
        (let [server-stop (:server @*components)]
          (server-stop)))
      (swap! *components select-keys [:js])
      (initialise-components! [{:name :ds
                                :init-fn (fn [_deps] (db/ds :master))
                                :deps []}
                               {:name :server
                                :deps [:ds :js]
                                :init-fn initialise-server!}]))
    (do
      (stop-server)
      (start-server))))

(comment
  (start-server)
  (stop-server)
  (restart-server)
  (restart-server :keep-js true)
  (def test-wrapper
    (util/wrap-json (fn [request] request)))
  (test-wrapper {:status 200
                 :body "{\"value\":\"Hello, Source!\"}"
                 :headers {"Content-Type" "application/json"}})
  ())
