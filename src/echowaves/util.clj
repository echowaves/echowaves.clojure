(ns echowaves.util
  (:require [noir.io :refer [resource-path]]
            [environ.core :refer [env]]
            [aws.sdk.s3 :as s3])
  (:import java.io.File))

(def thumb-prefix "thumb_")

(defn random-string [length]
  (let [ascii-codes (concat (range 48 58) (range 66 91) (range 97 123))]
    (apply str (repeatedly length #(char (rand-nth ascii-codes))))))

(defn generate-token []
  (random-string 7))

(defn send-ios-push-notification [message badge tokens]
  (javapns.Push/combined message
                         (read-string badge)
                         "default"
                         (env :ew-push-cert)
                         (env :ew-push-cert-pass)
                         (boolean (Boolean/valueOf (env :ew-push-prod)))
                         tokens))

(def aws-cred {:access-key (env :ew-aws-access-key)
               :secret-key (env :ew-aws-secret-key)})

(def aws-bucket-name (env :ew-aws-bucket-name))

