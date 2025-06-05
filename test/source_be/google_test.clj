(ns source-be.google-test
  (:require [clojure.test :refer :all]))

(deftest yeet-test
  (testing "adding numbers together"
    (with-redefs [])
    (is (= 69 (+ 60 9)))))

;; TODO: DELETE ALL OF THE BELOW WHEN YOU ARE DONE IN OAUTH2/GOOGLE
;; if this is in a different namespace
(def ^:private cache (atom {}))

(defn- -something-that-updates [*cache]
  (swap! *cache assoc (random-uuid) {:some "value"}))

(defn- -something-that-reads [*cache]
  (let [cache' @*cache]
    (swap! cache dissoc (second (last cache')))
    (if (> (count cache') 3)
      :success
      :fail)))

(comment
  (def ^:private test-cache (atom {}))
  (-something-that-updates test-cache)
  @cache
  (-something-that-reads test-cache))

(defprotocol AuthRequestCache
  (something-that-updates [this])
  (something-that-reads [this]))

(defn create-auth-request-cache [& _opts]
  (let [lcache (atom {})]
    (reify AuthRequestCache
      (something-that-updates [_]
        (-something-that-updates lcache))
      (something-that-reads [_]
        (-something-that-reads lcache)))))

(def auth-request-cache (create-auth-request-cache))

(something-that-updates auth-request-cache)


