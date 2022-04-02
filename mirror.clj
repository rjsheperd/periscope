#!/usr/bin/env bb

(defn retrieve [path]
  (let [filename (last path)
        dirs     (vec (butlast path))]
    (apply io/file (conj dirs (str/replace filename #"\.html" "")))))

(defn url-to-path [url]
  (-> url
      (str/split #"http(s)?://")
      (last)
      (str/split #"/")))

(defn html->gmi [file output]
  (io/make-parents (.getAbsolutePath output))
  (apply shell/sh ["html2gmi" "-i" (.getAbsolutePath file) "-o" (.getAbsolutePath output)]))

(defn download [url output-path]
  (let [tmp-path   (str/join "/" (concat ["tmp"] output-path))
        tmp-output (io/file tmp-path)]
    (println tmp-path)
    (io/make-parents tmp-path)
    (io/copy (:body (curl/get url {:as :bytes})) tmp-output)
    (html->gmi tmp-output (retrieve output-path))))

(defn mirror [url]
  (let [path (url-to-path url)
        file (retrieve path)]
    (when-not (.exists file) (download url path))))

(mirror "https://clojure.org/guides/spec")

(let [[url] *command-line-args*]
  (when (or (empty? url))
    (println "Usage: <url>")
    (System/exit 1))
  (mirror url))
