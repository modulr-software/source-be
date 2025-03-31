(ns source.middleware.auth
  (:require [source.auth :as auth]
            [clojure.string :as str]
            [source.util :as util]))



(def unauthorized-response {:status 403
                  :body {:message "Unathorized"}
                  :headers {"Content-Type" "application/json"}})

(defn wrap-auth [handler]
           (fn [request]
             (if-let [token (util/request->auth-token request)]
               (cond

                 (auth/validate-session token)
                 (handler request)

                 :else unauthorized-response)
               unauthorized-response)
             ))

(comment
  (def test-request {
                     :headers {"Content-Type" "application/json"
                               "Authorization" (str "Bearer " (auth/sign-jwt {:user "someone" }))}
                     :body {:data  "some super secret message"}
  })
  (defn test-handler [request]
    {:status 200 :body { :message "this is a massage "}})

  (let [wrapped-handler (wrap-auth test-handler)]
    (wrapped-handler test-request))
  )