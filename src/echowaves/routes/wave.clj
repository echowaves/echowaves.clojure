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
            [echowaves.routes.auth :as auth]
            [aws.sdk.s3 :as s3]))

;; this method does not really display the wave, but prepares the html page  
(defn display-wave [selected_wave_name]
  (if selected_wave_name
    (let [wave_name selected_wave_name]
    (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :aws-bucket   (env :ew-aws-bucket-name)
                  }))
    (let [wave_name (session/get :wave)]
    (layout/render "wave.html"
                 {:thumb-prefix thumb-prefix
                  :page-owner   wave_name
                  :aws-bucket   (env :ew-aws-bucket-name)
                  }))
    ) )


(defn display-wave-json [wave_name page_number]
  (if (db/is-child-wave (session/get :wave) wave_name)
    (noir.response/json (db/images-by-wave-blended wave_name page_number))
    (noir.response/status 401 (noir.response/json {:status "unathorized"}))))

(defn display-wave-details-json [wave_name]
  (if (db/is-child-wave (session/get :wave) wave_name)
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

(defn handle-delete-child-wave-json [wave_name]
  (let [parent_wave_name (session/get :wave)]

    (if (db/check-wave-belongs-to-parent parent_wave_name wave_name)
      (do
        (db/delete-wave wave_name)
        (s3/delete-object u/aws-cred u/aws-bucket-name (str "img/" wave_name))
        (noir.response/json {:status "deleted"}))
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defn handle-make-wave-active-json [wave_name active]
  (if (db/is-child-wave (session/get :wave) wave_name)
      (noir.response/json (db/make-wave-active wave_name active))
      (noir.response/status 401 (noir.response/json {:status "unathorized"}))))

(defroutes wave-routes
  (GET "/wave" []
       (restricted(display-wave nil)))
  (POST "/select-wave" [waves-picker]
        (restricted(display-wave waves-picker)))
  (GET "/wave.json" [wave_name page_number]
       (restricted(display-wave-json wave_name page_number)))
  (GET "/wave-details.json" [wave_name]
       (restricted(display-wave-details-json wave_name)))
  (POST "/create-child-wave.json" [name] 
        (restricted(handle-create-child-wave-json name)))
  (GET "/all-my-waves.json" []
       (restricted(display-all-my-waves-json)))
  (POST "/delete-child-wave.json" [wave_name] 
        (restricted(handle-delete-child-wave-json wave_name)))
  (POST "/make-wave-active.json" [wave_name active]
        (restricted(handle-make-wave-active-json wave_name active))))
