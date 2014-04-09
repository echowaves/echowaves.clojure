(ns echowaves.util
  (:require [noir.io :refer [resource-path]]
            [noir.session :as session]
            [environ.core :refer [env]]
            [echowaves.models.db :as db]
            [aws.sdk.s3 :as s3])
  (:import java.io.File))

(def thumb-prefix "thumb_")

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

;; (defn generate-token []
;;   (random-string 255))


(defn send-push-notification [message badge tokens]
  (javapns.Push/combined message
                         (read-string badge)
                         "default"
                         (env :ew-push-cert)
                         (env :ew-push-cert-pass)
                         (boolean (Boolean/valueOf (env :ew-push-prod)))
                         tokens))

(defn check-child-wave [wave_name]
  (let [parent_wave_name (session/get :wave)
        parent_wave (db/get-wave parent_wave_name)]
   (if (db/check-wave-belongs-to-parent parent_wave_name wave_name)
      true
      false)))

(def aws-cred {:access-key (env :ew-aws-access-key)
               :secret-key (env :ew-aws-secret-key)})

(def aws-bucket-name (env :ew-aws-bucket-name))

