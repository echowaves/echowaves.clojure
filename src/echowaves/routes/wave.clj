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
            [echowaves.util :as u]
            [echowaves.routes.auth :as auth]))

(defn display-wave []
  (let [wave_name (session/get :wave)]
    (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :aws-bucket   (env :ew-aws-bucket-name)
                  :images       (db/images-by-wave-blended wave_name)})))


(defn display-wave-json [wave_name]
  (if (u/check-child-wave wave_name)
    (noir.response/json (db/images-by-wave-blended wave_name))
    (noir.response/status 401 (noir.response/json {:status "unathorized"}))))

(defn display-wave-details-json [wave_name]
  (if (u/check-child-wave wave_name)
    (noir.response/json (db/get-wave-details wave_name))
    (noir.response/status 401 (noir.response/json {:status "unathorized"}))))

(defn display-all-my-waves-json []
  (let [wave_name (session/get :wave)] 
    (noir.response/json (db/child-waves wave_name))))

(defn valid? [name]
  (auth/valid_wave_name? name)
  (not (vali/errors? :name)))

(defn handle-create-child-wave-json [name]
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

(defn handle-delete-child-wave-json [name]
  (let [parent_wave_name (session/get :wave)
        parent_wave (db/get-wave parent_wave_name)]

    ;; if (db/check-wave-belongs-to-parent parent_wave_name wave_name)
    
    ;; check here if the wave belonds to the parent
    ;; 123123123
    ;; (db/delete-wave name)
    ;; (s3/delete-object u/aws-cred u/aws-bucket-name (str "/img/" name)))
    (noir.response/json {:status "deleted"})))

(defn handle-make-wave-active-json [wave_name active]
  (if (u/check-child-wave wave_name)
      (noir.response/json (db/make-wave-active wave_name active))
      (noir.response/status 401 (noir.response/json {:status "unathorized"}))))

(defroutes wave-routes
  (GET "/wave" []
       (restricted(display-wave)))
  (GET "/wave.json" [wave_name]
       (restricted(display-wave-json wave_name)))
  (GET "/wave-details.json" [wave_name]
       (restricted(display-wave-details-json wave_name)))
  (POST "/create-child-wave.json" [name] 
        (restricted(handle-create-child-wave-json name)))
  (GET "/all-my-waves.json" []
       (restricted(display-all-my-waves-json)))
  (POST "/delete-child-wave.json" [name] 
        (restricted(handle-delete-child-wave-json name)))
  (POST "/make-wave-active.json" [wave_name active]
        (restricted(handle-make-wave-active-json wave_name active))))


