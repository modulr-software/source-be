(ns source.middleware.interface
  (:require [source.middleware.core :as mw]))

(defn apply-generic [app & {:keys [ds store js]}]
  (mw/apply-generic app :ds ds :store store :js js))

(defn apply-auth
  "accepts required-type as an optional parameter to authorize the route only for the specified user type"
  [app & {:keys [required-type]}]
  (mw/apply-auth app {:required-type required-type}))

(defn apply-bundle [app]
  (mw/apply-bundle app))

(defn apply-api-key [app]
  (mw/apply-api-key app))
