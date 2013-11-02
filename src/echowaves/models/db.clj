(ns echowaves.models.db
  (:require [clojure.java.jdbc :as sql]
            [korma.db :refer [defdb transaction]]
            [korma.core :refer :all]
            [environ.core :refer [env]]))
(use 'korma.db)

(defdb db (mysql {
                  :host "localhost"
                  :port "3306"
                  :delimiters "`"
                  :db "echowaves"
                  :user (env :ew-db-user)
                  :password (env :ew-db-pass)}))


;; (defdb korma-db db)

(declare waves images)

(defentity waves
  (has-many images))

(defentity images
  (belongs-to waves))

(defn create-wave [wave]
  (insert waves (values wave)))

(defn get-wave [name]
  (first (select waves
                 (where {:name name})
                 (limit 1))))
                 
(defn delete-wave [name]
  (delete waves (where {:name name})))  

(defn add-image [wave_name name]  
  (transaction
   (let [wave (first(select waves
                            (fields :id)
                            (where {:name wave_name})
                            (limit 1)))]
     (if (empty? (select images 
                         (where {:waves_id (:id wave)
                                 :name name})
                         (limit 1)))
     (insert images (values {:waves_id (:id wave)
                             :name name}))
     (throw 
      (Exception. "you have already uploaded an image with the same name"))))))
                           
(defn images-by-wave [wave_name]
  (select images
          (where {:waves_id [in (subselect waves
                                           (fields :id)
                                           (where {:name wave_name}))]})
          (with waves)
          (order :created_on :DESC)
          ))

(defn delete-image [wave_name name]
  (delete images (where  {:name name} ))) 

(defn get-echowaves-previews []
  nil
  )
  ;; (select images
  ;;         (where {:waves_id [in (subselect waves
  ;;                                          (fields :id)
  ;;                                          )]})
  ;;         (with waves)
  ;;         (order :created_on :DESC)
  ;;         )) 
