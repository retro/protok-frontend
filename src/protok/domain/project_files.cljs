(ns protok.domain.project-files)

(defn url [project-file]
  (let [temp-url (:protok/temp-url project-file)]
    (if temp-url
      temp-url
      (let [server-filename (:serverFilename project-file)]
        (when (and server-filename (not (empty? server-filename)))
          (str "https://d2hqo2gp2sp39y.cloudfront.net/image/300x/" server-filename))))))
