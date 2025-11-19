(ns source.routes.jobs-view
  (:require [ring.util.response :as res]
            [congest.jobs :as congest]
            [clojure.walk :as walk]))

(defn stringify-unknowns [x]
  (walk/postwalk
   (fn [v]
     (if (or (map? v)
             (sequential? v)
             (string? v)
             (number? v)
             (boolean? v)
             (keyword? v)
             (nil? v))
       v
       (str v))) x))

(defn get [{:keys [js]}]
  (let [raw-jobs (congest/view js)
        formatted (stringify-unknowns raw-jobs)]
    (res/response formatted)))
