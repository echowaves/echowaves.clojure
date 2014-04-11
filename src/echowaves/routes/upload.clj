(ns echowaves.routes.upload  
  (:require [compojure.core :refer [defroutes GET POST]]
            [echowaves.views.layout :as layout]
            [hiccup.util :refer [url-encode]]
            [noir.io :refer [resource-path]]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]            
            [clojure.java.io :as io]
            [echowaves.models.db :as db]
            [echowaves.util :refer [thumb-prefix random-string]]
            [echowaves.util :as util]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]
            [aws.sdk.s3 :as s3])
  (:import [java.io File FileInputStream FileOutputStream]
           java.awt.image.BufferedImage
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           java.awt.image.AffineTransformOp
           javax.imageio.ImageIO))

(def thumb-size 600)

(defn scale [img ratio width height]  
  (let [scale        (AffineTransform/getScaleInstance 
                      (double ratio) (double ratio))
        transform-op (AffineTransformOp. 
                       scale AffineTransformOp/TYPE_BILINEAR)]    
    (.filter transform-op img (BufferedImage. width height (.getType img)))))

(defn scale-image [file]
  (let [img        (ImageIO/read file)
        img-width  (.getWidth img)
        img-height (.getHeight img)]
    (let [ratio (/ thumb-size img-height)]        
      (scale img ratio (int (* img-width ratio)) thumb-size))))

(defn save-thumbnail [path filename]
  (ImageIO/write 
    (scale-image (io/input-stream (str path filename))) 
    "jpeg" 
    (File. (str path thumb-prefix filename)))
  )

(defn cleanup-files [path filename]
  (.delete (File. (str path filename)))
  (.delete (File. (str path thumb-prefix filename)))
  )


(defn aws-delete-image [file wave]
  (info "................deleting " "img/" wave "/" file)
  (s3/delete-object util/aws-cred util/aws-bucket-name (str "img/" wave "/" file))  
  (s3/delete-object util/aws-cred util/aws-bucket-name (str "img/" wave "/" (str thumb-prefix file)))
  )


(defn aws-upload [path file wave]
  (s3/put-object util/aws-cred util/aws-bucket-name (str "img/" wave "/" file) (File. (str path file)) )
  (s3/put-object util/aws-cred util/aws-bucket-name (str "img/" wave "/" (str thumb-prefix file)) (File. (str path thumb-prefix file) ) )
  )

(defn upload-page [params]
  (layout/render "upload.html" params))

(defn handle-upload [file]
  (info "uploading file:" file)
  (upload-page
   (if (empty? (:filename file))
     {:error "please select a file to upload"}      
     (try
       (let [rnd-str (random-string 10)
             rand-path (str (resource-path) "img" File/separator rnd-str File/separator)]
         (.mkdir (File. rand-path))
         (noir.io/upload-file (str File/separator "img" File/separator rnd-str File/separator) file)
         (save-thumbnail rand-path (:filename file))
         ;; (info "random path: " rand-path)
         (future
           ;; have to iterate over the same collection twice, firts
           ;; time insert db records because it's fast and will allow
           ;; the users to have a percetion that there are new images
           (doseq [wave (db/get-active-child-waves (session/get :wave))]
             (db/add-image (:name wave) (:filename file)))
           ;; in the second iteration do actual image upload which may
           ;; take a while
           (doseq [wave (db/get-active-child-waves (session/get :wave))]
             (aws-upload rand-path (:filename file) (:name wave)))
           (cleanup-files rand-path (:filename file))
           (.delete (File. rand-path))           
           )


         ;; (shutdown-agents)
         )
       {:image
        (str "/img/" (session/get :wave) "/" thumb-prefix (url-encode (:filename file)))}        
       (catch Exception ex
         (error ex)
         {:error (str "error uploading file: " (.getMessage ex))})))
   )
  )

(defn handle-push-notify [wave_name badge]
  (util/send-push-notification
   (str "new images in wave: " wave_name)
   badge
   (db/get-blended-tokens (session/get :wave)))
  ;; (.start (Thread. (fn [] )))
  (resp/json {:status "OK"}))

(defn delete-image [wave_name name]
  (try
    (future (do
              (aws-delete-image name wave_name)
              ))
    (db/delete-image wave_name name)
    "ok"
    (catch Exception ex
      (error ex "an error has occured while deleting" name)
      (.getMessage ex)))
  )

(defn delete-images [names]
  (let [wave_name (session/get :wave)]
    (resp/edn
     (for [name names] {:name name :status (delete-image wave_name name)}))))

(defn handle-delete-image [name]
  (let [wave_name (session/get :wave)]    
    (resp/json {:name name :status (delete-image wave_name name)})))

(defroutes upload-routes
  (GET "/upload" [info] (upload-page {:info info}))
  
  (POST "/upload" [file] 
        (restricted (handle-upload file)))
  
  (POST "/send-push-notify.json" [wave_name badge] 
        (restricted (handle-push-notify wave_name badge)))
  
  (POST "/delete" [names] (restricted (delete-images names)))
  (POST "/delete-image.json" [name] (restricted (handle-delete-image name))))
