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
            [noir.util.route :refer [restricted]])
  (:import java.io.File))

(defn create-waves-path []
  (let [wave-path (File. (session-wave-path))]
    (if-not (.exists wave-path) (.mkdir wave-path))
    (str (.getAbsolutePath wave-path) File/separator)))

(defn valid? [name pass pass1]
  (vali/rule (vali/has-value? name)
             [:name "Wave name is required."])
  (vali/rule (re-matches #"^[a-zA-Z0-9-_]+$" name)
             [:name "Wave name can not contain spaces or special characters."])
  (vali/rule (vali/min-length? name 3)
             [:name "Wave name must be at least 3 characters."])
  (vali/rule (vali/max-length? name 50)
             [:name "Wave name must not be more then 50 characters."])
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

(defn handle-login [name pass]
  (let [wave (db/get-wave name)] 
    (if (and wave (crypt/compare pass (:pass wave)))
      (session/put! :wave name)))
  (resp/redirect "/"))

(defn handle-logout []
  (session/clear!)
  (resp/redirect "/"))

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
  
  (POST "/register" [name pass pass1] 
        (handle-registration name pass pass1))
  
  (POST "/login" [name pass] 
        (handle-login name pass))

  (GET "/logout" [] 
       (handle-logout))
  
  (GET "/delete-wave" [] 
       (restricted (delete-wave-page)))
  
  (POST "/confirm-delete" [] 
        (restricted (handle-confirm-delete))))
