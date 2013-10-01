(ns echowaves.models.db
  (:require [clojure.java.jdbc :as sql]
            [korma.db :refer [defdb transaction]]
            [korma.core :refer :all]
            [echowaves.models.schema :as schema]))

(defdb db schema/db-spec)

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
          (with waves)
          (where {:waves_id [in (subselect waves
                                                 (fields :id)
                                                 (where {:name wave_name}))]})))
                 
(defn delete-image [wave_name name]
  (delete images (where  {:name name} ))) 

(defn get-echowaves-previews []
  (exec-raw
   ["select waves.name as wave_name, rows.name as image_name, row_number() over() as r_num from (select *, row_number() over (partition by waves_id) as row_number from images) as rows inner join waves on rows.waves_id = waves.id where row_number = 1 order by r_num asc limit 100" []] ;; Show last 100 waves
     :results)) 
