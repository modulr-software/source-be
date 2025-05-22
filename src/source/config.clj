(ns source.config
  (:require [aero.core :as aero]
            [malli.core :as m]
            [malli.transform :as mt]
            [malli.error :as me]))

(def ^:private schema
  [:map
   [:supersecretkey [:string {:min 32}]]
   [:database-dir :string]])

(defn- load-config []
  (let [config (aero/read-config "config.edn")
        decoded (m/decode schema config mt/string-transformer)]
    (when-not (m/validate schema decoded)
      (throw (ex-info "invalid config"
                      (->> decoded
                           (m/explain schema)
                           (me/humanize)))))
    decoded))

(defn read-value
  "Loads in validated config and uses get-in with ks as an argument"
  [ks]
  (-> (load-config)
      (get-in ks)))

(comment
  (load-config))
