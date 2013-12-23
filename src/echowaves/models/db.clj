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

(declare waves images blends)

(defentity waves
  (has-many images))

(defentity images
  (belongs-to waves))

(defentity blends)


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



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; blending waves

(defn request-blending  [wave_id1 wave_id2]
  (insert blends (values {:wave_id1 wave_id1 :wave_id2 wave_id2})))


(defn confirm-blending [wave_id1 wave_id2]
(update blends
          (set-fields {:confirmed_on (sqlfn now)})
          (where {:wave_id1 wave_id1
                  :wave_id2 wave_id2})))

(defn unblend [wave_id1 wave_id2]
  (delete blends
          (where ( or
                   (and {:wave_id1 wave_id1
                         :wave_id2 wave_id2})
                   (and {:wave_id1 wave_id2
                         :wave_id2 wave_id1})))))

(defn blended-with [wave_id]
  (select waves
          (fields :id :name)
          (where (or
                  ;; select from blends
                  {:id [in (subselect blends
                                      (fields :wave_id1)
                                      (where (and (= :wave_id2 wave_id)
                                                  (not= :confirmed_on nil))))]}
                  ;; from both sides of blends
                  {:id [in (subselect blends
                                      (fields :wave_id2)
                                      (where (and (= :wave_id1 wave_id)
                                                  (not= :confirmed_on nil))))]}
                  ;; also select self
                  ;; {:id [in (subselect waves
                  ;;                     (fields :id)
                  ;;                     (where {:id wave_id}))]}
                  ))))
;; blends requests sent to wave_id, and waiting to be confirmed by wave_id
(defn requested-blends [wave_id]
  (select blends
          (where {:wave_id2 wave_id
                  :confirmed_on nil})))
;; blends requested by wave_id, and wating to be confirmed by other waves
(defn unconfirmed-blends [wave_id]
  (select blends
          (where {:wave_id1 wave_id
                  :confirmed_on nil})))

;; (defn get-blended-images [wave_id])
