(ns echowaves.routes.blends
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]
            [noir.io :refer [resource-path]]
            [noir.session :as session]
            [noir.response :as resp]            
            [clojure.java.io :as io]
            [echowaves.models.db :as db]
            [echowaves.util :as u]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            [noir.util.route :refer [restricted]]            )
  )

(defn handle-blended-with [wave_name]
  (let [wave (db/get-wave wave_name)]
    (if (u/check-child-wave wave_name)
      (noir.response/json (db/blended-with (:id wave)))
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defn handle-requested-blends [wave_name]
  (let [wave (db/get-wave wave_name)]
    (if (u/check-child-wave wave_name)
      (noir.response/json (db/requested-blends (:id wave)))
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))
    ))

(defn handle-unconfirmed-blends [wave_name]
  (let [wave (db/get-wave wave_name)]
    (if (u/check-child-wave wave_name)
      (noir.response/json (db/unconfirmed-blends (:id wave)))
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defn handle-autocomplete-wave-name [term]
  (debug "autocompleting: " term)
  (noir.response/json (db/autocomplete-wave-name term)))

(defn handle-request-blending [wave_name from_wave]
  (let [wave1 (db/get-wave from_wave)
        wave2 (db/get-wave wave_name)]
    (if (u/check-child-wave from_wave)
      (do
        (u/send-push-notification
         (str (:name wave1) " wants to blend with " (:name wave2))
         (str 1) 
         (db/get-tokens-for-wave (:name wave2)))
        (noir.response/json {:status (db/request-blending (:id wave1) (:id wave2))}))
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defn handle-confirm-blending [wave_name from_wave]
  (let [wave1 (db/get-wave from_wave)
        wave2 (db/get-wave wave_name)]
    (if (u/check-child-wave from_wave)
      (noir.response/json {:status (db/confirm-blending (:id wave1) (:id wave2))})
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defn handle-unblend [wave_name from_wave]
  (let [wave1 (db/get-wave from_wave)
        wave2 (db/get-wave wave_name)]
    (if (u/check-child-wave from_wave)
      (noir.response/json {:status (db/unblend (:id wave1) (:id wave2))})
      (noir.response/status 401 (noir.response/json {:status "unathorized"})))))

(defroutes blends-routes
  (GET "/blended-with.json" [wave_name]
       (restricted (handle-blended-with wave_name)))
  (GET "/requested-blends.json" [wave_name]
       (restricted (handle-requested-blends wave_name)))
  (GET "/unconfirmed-blends.json" [wave_name]
       (restricted (handle-unconfirmed-blends wave_name)))
  (GET "/autocomplete-wave-name.json" [term]
       (restricted (handle-autocomplete-wave-name term)))
  (POST "/request-blending.json" [wave_name from_wave]
        (restricted (handle-request-blending wave_name from_wave)))
  (POST "/confirm-blending.json" [wave_name from_wave]
        (restricted (handle-confirm-blending wave_name from_wave)))
  (POST "/unblend.json" [wave_name from_wave]
       (restricted (handle-unblend wave_name from_wave))))
