(ns source.server
  (:require [org.httpkit.server :as http]
            [source.db.interface :as db]
            [source.datastore.interface :as store]
            [congest.jobs :as congest]
            [source.jobs.core :as jobs]
            [source.routes.interface :as routes]
            [source.util :as util]))

(defonce ^:private *components (atom nil))

(defn initialise-server! [{:keys [ds store js]}]
  (http/run-server
   (routes/create-app {:ds ds
                       :store store
                       :js js})
   {:port 3000}))

(defn initialise-job-service! [{:keys [ds store] :as _deps}]
  (->> (jobs/interrupted-jobs ds store)
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
    (catch Exception e (println (str "Failed to initialise " name ":") e))))

(defn initialise-components! [components]
  (run! initialise! components))

(defn running? []
  (some? (:server @*components)))

(defn start-server []
  (cond (not (some? (:server @*components)))
        (do
          (println "Starting server on port 3000...")
          (initialise-components! [{:name :ds
                                    :init-fn (fn [_deps] (db/ds :master))
                                    :deps []}
                                   {:name :store
                                    :init-fn (fn [_deps] (store/ds :datahike))
                                    :deps []}
                                   {:name :js
                                    :deps [:ds :store]
                                    :init-fn initialise-job-service!}
                                   {:name :server
                                    :deps [:ds :store :js]
                                    :init-fn initialise-server!}]))
        :else
        (println "Server already running!")))

(defn stop-server []
  (println "Stopping server...")
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
                               {:name :store
                                :init-fn (fn [_deps] (store/ds :datahike))
                                :deps []}
                               {:name :server
                                :deps [:ds :store :js]
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
