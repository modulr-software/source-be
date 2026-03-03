(ns source.routes.jobs-view
  (:require [ring.util.response :as res]
            [congest.jobs :as congest]
            [clojure.walk :as walk]
            [source.routes.openapi :as api]))

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

(defn get
  {:summary "gets a list of all jobs"
   :responses (api/success [:vector [:map
                                     [:args [:map-of :string :any]]
                                     [:initial-delay :int]
                                     (api/sometimes :kill-after :int)
                                     (api/sometimes :auto-start :boolean)
                                     [:created-at :int]
                                     [:handler-name :string]
                                     [:num-calls :int]
                                     [:id :string]
                                     [:stop-after-fail :boolean]
                                     [:interval :int]
                                     [:recurring? :boolean]
                                     [:sleep :boolean]]])}
  [{:keys [js]}]
  (let [raw-jobs (congest/view js)
        formatted (mapv (fn [[_ v]] v) (stringify-unknowns raw-jobs))]
    (res/response formatted)))
