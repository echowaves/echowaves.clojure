(ns picture-gallery.routes.upload  
  (:require [compojure.core :refer [defroutes GET POST]]
            [picture-gallery.views.layout :as layout]
            [hiccup.util :refer [url-encode]]
            [noir.io :refer [resource-path]]
            [noir.session :as session]
            [noir.response :as resp]
            [noir.util.route :refer [restricted]]            
            [clojure.java.io :as io]
            [picture-gallery.models.db :as db]
            [picture-gallery.util :refer [gallery-path thumb-prefix]]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]])
  (:import [java.io File FileInputStream FileOutputStream]
           java.awt.image.BufferedImage
           java.awt.RenderingHints
           java.awt.geom.AffineTransform
           java.awt.image.AffineTransformOp
           javax.imageio.ImageIO))

(def thumb-size 150)

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

(defn save-thumbnail [{:keys [filename]}]
  (ImageIO/write 
    (scale-image (io/input-stream (str (gallery-path) filename))) 
    "jpeg" 
    (File. (str (gallery-path) thumb-prefix filename))))

(defn upload-page [params]
  (layout/render "upload.html" params))

(defn handle-upload [file]
  (upload-page 
    (if (empty? (:filename file))
      {:error "please select a file to upload"}      
      (try 
        (noir.io/upload-file          
          (str File/separator "img" File/separator (session/get :user) File/separator)
          file)
        (save-thumbnail file)
        (db/add-image (session/get :user) (:filename file))
        {:image
         (str "/img/" (session/get :user) "/" thumb-prefix (url-encode (:filename file)))}
        (catch Exception ex 
          {:error (str "error uploading file: " (.getMessage ex))})))))

(defn delete-image [userid name]
  (try
    (db/delete-image userid name)
    (io/delete-file (str (gallery-path) name))
    (io/delete-file (str (gallery-path) thumb-prefix name))
    "ok"
    (catch Exception ex
      (error ex "an error has occured while deleting" name)
      (.getMessage ex))))

(defn delete-images [names]
  (let [userid (session/get :user)]
    (resp/edn
      (for [name names] {:name name :status (delete-image userid name)}))))

(defroutes upload-routes
  (GET "/upload" [info] (upload-page {:info info}))
  
  (POST "/upload" [file] (restricted (handle-upload file)))
  
  (POST "/delete" [names] (restricted (delete-images names))))