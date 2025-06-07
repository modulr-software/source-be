(ns source-be.google-test
  (:require [clojure.test :refer :all]
            [source.cache :as cache]
            [source.oauth2.google :as google]
            [clj-oauth2.client :as oauth2]))


(deftest google-auth-flow-test
  (testing "testing the google oauth2 flow"
    (with-redefs [])
    (let [auth-reqs-service (cache/create-cache)]
      (google/-google-session-user )     
      )))


(deftest yeet-test
  (testing "adding numbers together"
    (with-redefs [])
    (is (= 69 (+ 60 9)))))

