(ns echowaves.test.handler
  (:require [clojure.test :refer :all]        
            [ring.mock.request :refer :all]
            [noir.util.crypt :refer [encrypt]]
            [echowaves.handler :refer :all]
            [noir.util.test :as ntest]))

(defn mock-get-wave [name]
  (if (= name "foo") 
    {:name "foo" :pass (encrypt "12345")}))
    
(deftest test-login  
  (testing "login success"
    (with-redefs [echowaves.models.db/get-wave mock-get-wave]
      (is 
        (-> (request :post "/login" {:name "foo" :pass "12345"}) 
          app :headers (get "Set-Cookie") not-empty))))
  
  (testing "password mismatch"
    (with-redefs [echowaves.models.db/get-wave mock-get-wave]
      (is 
        (-> (request :post "/login" {:name "foo" :pass "123456"}) 
          app :headers (get "Set-Cookie") empty?))))
  
  (testing "wave not found"
    (with-redefs [echowaves.models.db/get-wave mock-get-wave]
      (is 
        (-> (request :post "/login" {:name "bar" :pass "12345"}) 
          app :headers (get "Set-Cookie") empty?)))))

(deftest test-signup-validation
  (testing "signup validation successfull"
    (is
     (echowaves.routes.auth/valid? "dmitry" "dmitry" "dmitry"))
    )
  )
