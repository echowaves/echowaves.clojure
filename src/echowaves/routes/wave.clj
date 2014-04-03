(ns echowaves.routes.wave
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]
            [echowaves.util :refer [thumb-prefix]]
            [echowaves.models.db :as db]
            [noir.session :as session]
            [noir.validation :as vali]
            [noir.util.route :refer [restricted]]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            [environ.core :refer [env]]
            [echowaves.routes.auth :as auth]))

(defn display-wave []
  (let [wave_name (session/get :wave)]
    (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :aws-bucket   (env :ew-aws-bucket-name)
                  :images       (db/images-by-wave-blended wave_name)})))


(defn display-wave-json []
  (let [wave_name (session/get :wave)] 
    (noir.response/json (db/images-by-wave-blended wave_name))))

(defn display-child-waves-json []
  (let [wave_name (session/get :wave)] 
    (noir.response/json (db/child-waves wave_name))))

(defn valid? [name]
  (auth/valid_wave_name? name)
  (not (vali/errors? :name)))

(defn handle-create-child-wave-json [name]
  (info "handle-create-child-wave-json" name)
  (if (valid? name)
    
    (let [parent_wave_name (session/get :wave)]
      (try        
        (db/create-child-wave parent_wave_name name)      
        (noir.response/json {:wave name})
        (catch Exception ex
          (info "error" ex)
          (noir.response/status 409 (noir.response/json {:error "Duplicate wave name."})))))
    ;; validation errors here
    (do
      (info "errors happened:" (vali/get-errors))
      (noir.response/status 412 (noir.response/json {:error (vali/get-errors)})))
    ))

(defroutes wave-routes
  (GET "/wave" []
       (restricted(display-wave)))
  (GET "/wave.json" []
       (restricted(display-wave-json)))
  (POST "/create-child-wave.json" [name] 
        (restricted(handle-create-child-wave-json name)))
  (GET "/child-waves.json" []
       (restricted(display-child-waves-json))))


