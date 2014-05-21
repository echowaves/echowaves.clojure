(ns echowaves.models.db
  (:require [echowaves.util :as u]
            [noir.session :as session]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [korma.db :refer [defdb transaction]]
            [korma.core :refer :all]
            [environ.core :refer [env]]
            [taoensso.timbre 
             :refer [trace debug info warn error fatal]]))
(use 'korma.db)

(defdb db (mysql {
                  :host "localhost"
                  :port "3306"
                  :delimiters "`"
                  :db "echowaves"
                  :user (env :ew-db-user)
                  :password (env :ew-db-pass)}))


;; (defdb korma-db db)



(declare waves child_waves images blends)

(defentity waves
  (has-many images)
  (has-many child_waves))

(defentity child_waves
  (table :waves)
  ;; (has-many images)
  (belongs-to waves {:fk :parent_wave_id}))


(defentity images
  (belongs-to waves))

(defentity blends)

(defentity ios_tokens
  (belongs-to waves))

(defentity share_actions
  (belongs-to waves))


(defn get-wave-id [name]
  (:id (first (select waves
                 (fields :id)
                 (where {:name name})
                 (limit 1)))))

(defn get-image-id [waves_id image_name]
  (:id (first (select images
                 (fields :id)
                 (where {:name image_name
                         :waves_id waves_id})
                 (limit 1)))))

;; (defn get-wave-or-parent-id [name]
;;   (let [wave (first (select waves
;;                  (fields :id :parent_wave_id)
;;                  (where {:name name})
;;                  (limit 1)))]
;;     (if (:parent_wave_id wave)
;;       (:parent_wave_id wave)
;;       (:id wave)
;;       )))

(defn create-wave [wave]
  (insert waves (values wave)))

(defn create-child-wave [parent_wave_name child_wave_name]
  (let [parent_wave_id (get-wave-id parent_wave_name)]
    (insert waves (values {:parent_wave_id parent_wave_id
                           :name child_wave_name
                           :pass ""}))))

(defn check-wave-belongs-to-parent [parent_wave_name wave_name]
  (let [parent_wave_id (get-wave-id parent_wave_name)]
    (let [wave (first (select waves
                               (where {:name wave_name})
                               (limit 1)))]
      (if (or
           (and
            (not= nil (:parent_wave_id wave)) (== parent_wave_id (:parent_wave_id wave)))
           (and
            (not= nil (:id wave)) (== parent_wave_id (:id wave))))
        true
        false)
      )))

(defn make-wave-active [wave_name status]
  (update waves
          (set-fields {:active status})
          (where {:name wave_name}))
  {:status  (str "wave made active " status) }
  )

(defn get-wave-details [wave_name]
  (first (select waves
                 (fields :id :name :active :parent_wave_id)
                 (where (or
                         {:name wave_name}
                         )))))


(defn create-ios-token [name token]
  (let [wave-id (get-wave-id name)]
    (if (= (count (select ios_tokens (where {:waves_id wave-id
                                             :token token}))
                      ) 0)
      (insert ios_tokens (values {:waves_id wave-id :token token})))
    ))

(defn get-wave [name]
  (first (select waves
                 (where {:name name})
                 (limit 1))))



(defn delete-wave [name]
  (let [wave-id (get-wave-id name)]
    (delete images (where {:waves_id wave-id}))
    (delete blends (where {:wave_id1 wave-id}))
    (delete blends (where {:wave_id2 wave-id}))
    (delete waves (where {:id wave-id}))))  

(defn add-image [wave_name name]  
  (transaction
   (let [wave-id (get-wave-id wave_name)]
     (if (empty? (select images 
                         (where {:waves_id wave-id
                                 :name name})
                         (limit 1)))
     (insert images (values {:waves_id wave-id
                             :name name}))
     (throw 
      (Exception. "you have already uploaded an image with the same name"))))))

(declare blended-with)

(defn images-by-wave-blended [wave_name]
  (let [wave-id (get-wave-id wave_name)
        blended-with-map (map :id (blended-with wave-id))]
    
    (select images
            (where (or {:waves_id wave-id}
                       {:waves_id [in blended-with-map]}))
            (with waves
                  (fields :name :created_on))
            (order :name :DESC)
            (limit (* 100 (if-not (empty? blended-with-map)
                            (count blended-with-map)
                            1
                            )
                      )))))

(defn images-by-wave [wave_name]
  (let [wave-id (get-wave-id wave_name)]
    (select images
            (where {:waves_id wave-id})
            (with waves)
            (order :id :DESC))))

(defn child-waves [wave_name]
  (let [wave-id (get-wave-id wave_name)]
    (select waves
            (fields :id :name :active)
            (where (or
                    {:id wave-id}
                    {:parent_wave_id wave-id}))
            (order :id :ASC))))

(defn get-active-child-waves [wave_name]
  (let [wave-id (get-wave-id wave_name)]
    (select waves
            (fields :id :name)
            (where (and
                    {:active true}
                    (or
                     {:id wave-id}
                     {:parent_wave_id wave-id})) )
            (order :id :ASC))))


(defn delete-image [wave_name name]
  (let [wave-id (get-wave-id wave_name)]
    (delete images (where {:name name
                           :waves_id wave-id}))))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; blending waves

(defn confirm-blending [wave_id1 wave_id2]
  (debug "confirm-blending " wave_id1 wave_id2)
  (update blends
          (set-fields {:confirmed_on (sqlfn now)})
          (where {:wave_id1 wave_id1
                  :wave_id2 wave_id2}))
  (update blends
          (set-fields {:confirmed_on (sqlfn now)})
          (where {:wave_id1 wave_id2
                  :wave_id2 wave_id1})))

(defn request-blending  [wave_id1 wave_id2]
  (debug "requesting blending" wave_id1 wave_id2)
  (if-not (= wave_id1 wave_id2) 
    (do
      (if (> (count (select blends (where {:wave_id1 wave_id2
                                             :wave_id2 wave_id1}))
                      ) 0)
        (confirm-blending wave_id1 wave_id2)
        (insert blends (values {:wave_id1 wave_id1 :wave_id2 wave_id2}))))))

(defn unblend [wave_id1 wave_id2]
  (delete blends
          (where ( or
                   (and {:wave_id1 wave_id1
                         :wave_id2 wave_id2})
                   (and {:wave_id1 wave_id2
                         :wave_id2 wave_id1})))))

(defn blended-with [wave_id]
  (select waves
          (fields :id :parent_wave_id :name)
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
          (fields :waves.id :waves.name)
          (where {:wave_id2 wave_id
                  :confirmed_on nil})
          (join waves (= :waves.id :wave_id1))))
;; blends requested by wave_id, and wating to be confirmed by other waves
(defn unconfirmed-blends [wave_id]
  (select blends
          (fields :waves.id :waves.name)
          (where {:wave_id1 wave_id
                  :confirmed_on nil})
          (join waves (= :waves.id :wave_id2))))

;; autocomplete waves names
(defn autocomplete-wave-name [wave_name]
  (select waves
          (fields  [:name :label])
          ;; must return :label :value pair for autocomplete to work
          (where (like (raw "LOWER(name)")  (str "%" (str/lower-case wave_name) "%")))
          (order :name :ASC)
          (limit 10))
  )

(defn get-blended-ids [wave_name]
  (mapv (fn [y] (if (:parent_wave_id y)
                  (:parent_wave_id y)
                  (:id y)))
        (blended-with (get-wave-id wave_name))))

(defn get-blended-tokens [wave_name]
  (mapv (fn [y] (:token y))
        (select ios_tokens
          (with waves)
          (fields :token)
          (where {:waves_id [in (get-blended-ids wave_name)]})
          ))
  )

(defn get-tokens-for-wave [wave_name]
  (mapv (fn [y] (:token y))
        (select ios_tokens
          (with waves)
          (fields :token)
          (where {:waves_id (get-wave-id wave_name)})
          ))
  )


(defn is-child-wave [wave_in_session wave_name]
  (if (check-wave-belongs-to-parent wave_in_session wave_name)
      true
      false))


(defn share-image [wave_name image_name]
  (let [wave-id (get-wave-id wave_name)
        token (u/generate-token) ]
    (insert share_actions (values {
                                   :images_id (get-image-id wave-id image_name)
                                   :token token}))
    token))

;; (defn image-by-token [token session_wave]
;;   (let [share_action ((select share_actions (where {:token token}) (limit 1)) 0) ]
;;     (transaction
;; ;;     (delete share_actions (where {:token token}))
;;      (let [image ((select images
;;               (with waves
;;                     (fields :name :created_on))
;;               (where {:id (:images_id share_action)})
;;               (limit 1)) 0)
;;            wave_id_to   (get-wave-id session_wave)
;;            wave_id_from (:waves_id image)]
;;        (request-blending wave_id_from wave_id_to)
;;        image))))

(defn image-by-token [token]
  (let [share_action ((select share_actions (where {:token token}) (limit 1)) 0) ]
    (transaction
     (delete share_actions (where {:token token}))
     ((select images
              (with waves
                    (fields :name :created_on))
              (where {:id (:images_id share_action)})
              (limit 1)) 0))))
