(ns protok.controllers.image-uploader
  (:require [keechma.toolbox.pipeline.core :as pp :refer-macros [pipeline!]]
            [keechma.toolbox.pipeline.controller :as pp-controller]
            [keechma.toolbox.dataloader.controller :refer [wait-dataloader-pipeline!]]
            [protok.domain.db :as db]
            [promesa.core :as p]
            [protok.edb :as edb]
            [protok.gql :as gql]
            [oops.core :refer [oget ocall oset!]]
            [keechma.toolbox.forms.core :refer [id-key]]))


(defn file->base64 [file]
  (let [reader (js/FileReader.)]
    (p/promise 
     (fn [resolve reject]
       (ocall reader :readAsDataURL file)
       (oset! reader :onload #(resolve (oget reader :result)))
       (oset! reader :onerror reject)))))

(defn uploader! [value on-progress ctrl app-db-atom _]
  (let [{:keys [url file]} value]
    (p/promise
     (fn [resolve reject]
       (let [xhr (js/XMLHttpRequest.)
             mime-type (oget file "type")]

         (ocall xhr "upload.addEventListener" "progress"
                (fn [e]
                  (let [app-db @app-db-atom
                        new-app-db (on-progress e value app-db)]
                    (when (not= app-db new-app-db)
                      (reset! app-db-atom new-app-db))))
                false)

         (oset! xhr "onreadystatechange"
                (fn []
                  (when (= 4 (oget xhr "readyState"))
                    (if (= 200 (oget xhr "status"))
                      (resolve)
                      (reject (ex-info "File Upload Failed" {}))))))

         (ocall xhr "open" "PUT" url)
         (ocall xhr "setRequestHeader" "Content-Type" mime-type)
         (ocall xhr "send" file))))))

(defn upload-s3!
  ([value] (upload-s3! value (fn [_ value app-db] app-db)))
  ([value on-progress]
   (with-meta (partial uploader! value on-progress) {:pipeline? true})))

(defn get-file-info [file]
  {:filename (oget file :name)
   :mime-type (oget file :type)})

(defn create-project-file [app-db data]
  (let [project (edb/get-named-item app-db :project :current)]
    (gql/m! [:create-project-file :createProjectFile]
            {:input {:filename (get-in data [:info :filename])
                     :mime-type (get-in data [:info :mime-type])
                     :project-id (:id project)}}
            (db/get-jwt app-db))))

(defn insert-file-into-project-files [app-db project-file]
  (let [project (edb/get-named-item app-db :project :current)]
    (edb/prepend-related-collection app-db :project :projectFiles project [project-file])))

(def controller
  (pp-controller/constructor
   (constantly true)
   {:upload (pipeline! [value app-db]
              (assoc value :info (get-file-info (:file value)))
              (->> (file->base64 (:file value))
                   (p/map #(assoc value :protok/temp-url %)))
              (->> (create-project-file app-db value)
                   (p/map #(assoc value :project-file %)))
              (assoc-in value [:project-file :protok/temp-url] (:protok/temp-url value))
              (pp/commit! (insert-file-into-project-files app-db (:project-file value)))
              (pp/send-command! [id-key :on-change] [(:form-props value) (:path value) nil (:project-file value) nil])
              (upload-s3! {:file (:file value)
                           :url (get-in value [:project-file :uploadUrl])}
                          (fn [e _ app-db]
                            (let [id (get-in value [:project-file :id])]
                              (edb/insert-item app-db :project-file {:id id :protok/progress (/ (oget e :loaded) (oget e :total))})))))}))

(defn register
  ([] (register {}))
  ([controllers] (assoc controllers :image-uploader controller)))
