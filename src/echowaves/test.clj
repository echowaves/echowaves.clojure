(ns echowaves.test
  (:require [compojure.route :as route]
            [compojure.core :refer [defroutes]]
            [noir.util.middleware :as noir-middleware]
            [echowaves.routes.auth :refer [auth-routes]]
            [echowaves.routes.home :refer [home-routes]]
            [echowaves.routes.upload :refer [upload-routes]]
            [echowaves.routes.wave :refer [wave-routes]]
            [echowaves.routes.blends :refer [blends-routes]]
            [noir.session :as session]
            [taoensso.timbre :as timbre]
            [com.postspectacular.rotor :as rotor]
            [ring.middleware.format :refer [wrap-restful-format]]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            ))

(defn pushalert []
  (javapns.Push/alert "New images posted!" "/Users/dmitry/certificates/EWPush-dev.p12" "EWPush" false ["2281798df40beafd136e680fe2e35d392e43d814425e9efa538ea20e494e4f0a"])
  )
(defn pushbadge []
  (javapns.Push/badge 101 "/Users/dmitry/certificates/EWPush-dev.p12" "EWPush" false ["2281798df40beafd136e680fe2e35d392e43d814425e9efa538ea20e494e4f0a"])
  )
(defn pushsound []
  (javapns.Push/sound "default" "/Users/dmitry/certificates/EWPush-dev.p12" "EWPush" false ["2281798df40beafd136e680fe2e35d392e43d814425e9efa538ea20e494e4f0a"])
  )

(defn pushcombined []
  (javapns.Push/combined "New Images Posted" 1 "default" "/Users/dmitry/certificates/EWPush-dev.p12" "EWPush" false ["2281798df40beafd136e680fe2e35d392e43d814425e9efa538ea20e494e4f0a"])
  )

(defn push []
  (pushalert)
  (pushsound))

