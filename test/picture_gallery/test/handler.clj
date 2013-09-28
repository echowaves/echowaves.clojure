(ns echowaves.test.handler
  (:require [clojure.test :refer :all]        
            [ring.mock.request :refer :all]
            [noir.util.crypt :refer [encrypt]]
            [echowaves.handler :refer :all]))

(defn mock-get-user [id]
  (if (= id "foo") 
    {:id "foo" :pass (encrypt "12345")}))
    
(deftest test-login  
  (testing "login success"
    (with-redefs [echowaves.models.db/get-user mock-get-user]
      (is 
        (-> (request :post "/login" {:id "foo" :pass "12345"}) 
          app :headers (get "Set-Cookie") not-empty))))
  
  (testing "password mismatch"
    (with-redefs [echowaves.models.db/get-user mock-get-user]
      (is 
        (-> (request :post "/login" {:id "foo" :pass "123456"}) 
          app :headers (get "Set-Cookie") empty?))))
  
  (testing "user not found"
    (with-redefs [echowaves.models.db/get-user mock-get-user]
      (is 
        (-> (request :post "/login" {:id "bar" :pass "12345"}) 
          app :headers (get "Set-Cookie") empty?)))))
