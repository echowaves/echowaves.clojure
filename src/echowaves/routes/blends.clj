(ns echowaves.routes.blends
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]
            [noir.io :refer [resource-path]]
            [noir.session :as session]
            [noir.response :as resp]            
            [clojure.java.io :as io]
            [echowaves.models.db :as db]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            [noir.util.route :refer [restricted]]            )
  )

(defn handle-blended-with []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/blended-with (:id wave)))))
(defn handle-requiested-blends []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/requested-blends (:id wave)))))
(defn handle-unconfirmed-blends []
  (let [wave (db/get-wave (session/get :wave))]
    (noir.response/json (db/unconfirmed-blends (:id wave)))))
(defn handle-autocomplete-wave-name [term]
  (debug "autocompleting: " term)
  (noir.response/json (db/autocomplete-wave-name term)))

(defn handle-request-blending [wave_name]
  (debug "requesting blending for: " wave_name)
  (let [wave1 (db/get-wave (session/get :wave))
        wave2 (db/get-wave wave_name)]    
    (noir.response/json {:status (db/request-blending (:id wave1) (:id wave2))})))
(defn handle-confirm-blending [wave_name]
  (debug "confirming blending " wave_name)
  (let [wave1 (db/get-wave (session/get :wave))
        wave2 (db/get-wave wave_name)]
    (noir.response/json {:status (db/confirm-blending (:id wave1) (:id wave2))})))
(defn handle-unblend [wave_name]
  (debug "unblending: " wave_name)
  (let [wave1 (db/get-wave (session/get :wave))
        wave2 (db/get-wave wave_name)]
    (noir.response/json {:status (db/unblend (:id wave1) (:id wave2))})))

(defroutes blends-routes
  (GET "/blended-with.json" []
       (restricted (handle-blended-with)))
  (GET "/requested-blends.json" []
       (restricted (handle-requiested-blends)))
  (GET "/unconfirmed-blends.json" []
       (restricted (handle-unconfirmed-blends)))
  (GET "/autocomplete-wave-name.json" [term]
       (restricted (handle-autocomplete-wave-name term)))
  (POST "/request-blending.json" [wave_name]
        (restricted (handle-request-blending wave_name)))
  (POST "/confirm-blending.json" [wave_name]
        (restricted (handle-confirm-blending wave_name)))
  (POST "/unblend.json" [wave_name]
       (restricted (handle-unblend wave_name))))
