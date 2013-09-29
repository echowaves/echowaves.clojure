(ns echowaves.models.db
  (:require [clojure.java.jdbc :as sql]
            [korma.db :refer [defdb transaction]]
            [korma.core :refer :all]
            [echowaves.models.schema :as schema]))

(defdb db schema/db-spec)

;; (defdb korma-db db)

(defentity waves)

(defentity images)

(defn create-wave [wave]
  (insert waves (values wave)))

(defn get-wave [id]
  (first (select waves
                 (where {:id id})
                 (limit 1))))
                 
(defn delete-wave [id]
  (delete waves (where {:id id})))  

(defn add-image [wave_id name]  
  (transaction
    (if (empty? (select images 
                        (where {:wave_id wave_id :name name})
                        (limit 1)))
      (insert images (values {:wave_id wave_id :name name}))
      (throw 
        (Exception. "you have already uploaded an image with the same name")))))
                           
(defn images-by-wave [wave_id]
  (select images (where {:wave_id wave_id})))
                 
(defn delete-image [wave_id name]
  (delete images (where {:wave_id wave_id :name name}))) 

(defn get-echowaves-previews []
  (exec-raw
    ["select *, row_number() over() as r_num from (select *, row_number() over (partition by wave_id) as row_number from images) as rows where row_number = 1 order by r_num desc limit 100" []] ;; Show last 100 waves
     :results)) 
