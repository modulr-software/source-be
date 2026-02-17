(ns source.middleware.interface
  (:require [source.middleware.core :as mw]))

(defn apply-generic [app & {:keys [ds js]}]
  (mw/apply-generic app :ds ds :js js))

(defn apply-auth
  "accepts required-type as an optional parameter to authorize the route only for the specified user type"
  [app & {:keys [required-type]}]
  (mw/apply-auth app {:required-type required-type}))

(defn apply-bundle [app]
  (mw/apply-bundle app))

(defn apply-validation [app openapi-meta]
  (mw/wrap-input-validation app openapi-meta))
