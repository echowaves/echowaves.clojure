(ns echowaves.routes.auth
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]            
            [noir.session :as session]
            [noir.response :as resp]
            [noir.validation :as vali]
            [noir.util.crypt :as crypt]
            [echowaves.models.db :as db]
            [echowaves.util :refer [session-wave-path]]
            [echowaves.routes.upload :refer [delete-image]]
            [noir.util.route :refer [restricted]]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            )
  (:import java.io.File))

(defn create-waves-path []
  (let [wave-path (File. (session-wave-path))]
    (if-not (.exists wave-path) (.mkdir wave-path))
    (str (.getAbsolutePath wave-path) File/separator)))

(defn valid_wave_name? [name]
  (vali/rule (vali/has-value? name)
             [:name "Wave name is required."])
  (vali/rule (re-matches #"^[a-zA-Z0-9-_]+$" name)
             [:name "Wave name can not contain spaces or special characters."])
  (vali/rule (vali/min-length? name 3)
             [:name "Wave name must be at least 3 characters."])
  (vali/rule (vali/max-length? name 50)
             [:name "Wave name must not be more then 50 characters."]))

(defn valid? [name pass pass1]
  (valid_wave_name? name)
  (vali/rule (vali/min-length? pass 5)
             [:pass "Password must be at least 5 characters."])
  (vali/rule (vali/max-length? pass 50)
             [:pass "Password must not be more then 50 characters."])
  (vali/rule (= pass pass1)
             [:pass "Entered passwords do not match."])
  (not (vali/errors? :name :pass :pass1)))

(defn registration-page [& [name]]
  (layout/render "registration.html"
                 {:name name
                  :name-error (first (vali/get-errors :name))
                  :pass-error (first (vali/get-errors :pass))}))

(defn format-error [id ex]
  "An error has occured while processing the request")

;; toberemoved
(defn handle-registration [name pass pass1]
  (if (valid? name pass pass1)
    (try        
      (db/create-wave {:name name :pass (crypt/encrypt pass)})      
      (session/put! :wave name)
      (create-waves-path)
      (resp/redirect "/")
      (catch Exception ex
        (vali/rule false [:name (format-error name ex)])
        (registration-page)))
    (registration-page name)))

(defn handle-registration-json [name pass pass1]
  (info "handle-registration-json" name pass pass1)
  (if (valid? name pass pass1)
    (try        
      (db/create-wave {:name name :pass (crypt/encrypt pass)})      
      (session/put! :wave name)
      (create-waves-path)
      (noir.response/json {:wave name})
      (catch Exception ex
        (info "error" ex)
        (noir.response/status 409 (noir.response/json {:error "Duplicate wave name."}))))
    ;; validation errors here
    (do
      (info "errors happened:" (vali/get-errors))
      (noir.response/status 412 (noir.response/json {:error (vali/get-errors)})))
    ))

(defn handle-registration-ios-token-json [name token]
  (debug "handle-registration-ios-token-json" name token)
  (db/create-ios-token name token)
  (noir.response/json {:OK "Token created"}))

;; toberemoved
(defn handle-login [name pass]
  (let [wave (db/get-wave name)] 
    (if (and wave (crypt/compare pass (:pass wave)))
      (session/put! :wave name)))
  (resp/redirect "/"))

(defn handle-login-json [name pass]
  (let [wave (db/get-wave name)] 
    (if (and wave (crypt/compare pass (:pass wave)))
      (do
        (info "tuninig in: " session/get)
        (session/put! :wave name)
        (noir.response/json {:wave name}))
      (noir.response/status 401 (noir.response/json {:error "Wrong wave or password, try again."})))))

;; toberemoved
(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

(defn handle-logout-json []
  (info "tuninig out: " session/get)
  (session/clear!)
  (noir.response/json {:status "tunedOut"}))

(defn delete-wave-page []  
  (layout/render "deleteWave.html"))

(defn handle-confirm-delete []
  (let [wave_name (session/get :wave)] 
    (doseq [{:keys [name]} (db/images-by-wave wave_name)]      
      (delete-image wave_name name))    
    (clojure.java.io/delete-file (session-wave-path))
    (db/delete-wave wave_name))
  (session/clear!)
  (resp/redirect "/"))

(defroutes auth-routes 
  (GET "/register" [] 
       (registration-page))

  ;; toberemoved  
  (POST "/register" [name pass pass1] 
        (handle-registration name pass pass1))

  (POST "/register.json" [name pass pass1] 
        (handle-registration-json name pass pass1))
  (POST "/register-ios-token.json" [name token] 
        (handle-registration-ios-token-json name token))

  ;; toberemoved    
  (POST "/login" [name pass] 
        (handle-login name pass))

  (POST "/login.json" [name pass] 
        (handle-login-json name pass))

  ;; toberemoved  
  (GET "/logout" [] 
       (handle-logout))

  (POST "/logout.json" [] 
       (handle-logout-json))

  ;; toberemoved
  (GET "/delete-wave" [] 
       (restricted (delete-wave-page)))
  
  ;; Toberemoved
  (POST "/confirm-delete" [] 
        (restricted (handle-confirm-delete))))
