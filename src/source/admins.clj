(ns source.admins
  (:require [source.crypt-fs :as crypt]
            [source.config :as conf]
            [clojure.data.json :as json]
            [taoensso.telemere :as t]))

(defn encrypt! []
  (crypt/write-file-crypt! (conf/read-value :admins-encrypted-path)
                           (conf/read-value :supersecretkey)
                           (slurp (conf/read-value :admins-path))))

(defn read
  "reads admin user information from file"
  []
  (try
    (-> (conf/read-value :admins-encrypted-path)
        (crypt/read-file-crypt (conf/read-value :supersecretkey))
        (json/read-json))
    (catch Exception e
      (t/log! {:level :error
               :msg (str "Couldn't read the admins file: " (.getMessage e))})
      [])))
