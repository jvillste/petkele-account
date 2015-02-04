(ns petkele-account.csv
  (:require clojure.string))

(defn get-value [values n]
  (if (> (count values) n)
    (clojure.string/replace (nth values n) "\"" "")
    nil))

(defn read-csv-lines [file-name] (with-open [rdr (clojure.java.io/reader file-name)]
                                   (map #(clojure.string/split %  #";" #_#",") (doall (line-seq rdr)))))












