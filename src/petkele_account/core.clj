(ns petkele-account.core
  (:require clojure.string
            [petkele-account.csv :as csv]))

(defn date-string-to-tappio-date [date-string]
  (let [parts (clojure.string/split date-string #"\.")]
    (str "(date " (nth parts 2) " " (nth parts 1) " " (nth parts 0) ")")))

(defn format-amount [amount]
  (format "%.2f" (float amount)))

(defn amount-to-tappio-amount [amount]
  (-> amount
      (clojure.string/replace "," ".")
      (read-string)
      (format-amount)
      (clojure.string/replace "." "")))

(defn payment [account-number amount]
  (str "(" account-number " (money " amount "))"))

(defn negate-tappio-amount [amount]
  (if (= \- (first amount))
    (apply str (rest amount))
    (str "-" amount)))

(defn transfer [from target amount]
  (str "("
       (payment from (negate-tappio-amount amount))
       " "
       (payment target amount)
       ")"))

(defn read-events [file-name]
  (with-open [rdr (clojure.java.io/reader file-name)]
    (for [values (map #(clojure.string/split % #";") (doall (rest (line-seq rdr))))
          :when (> (count values)
                   2)]
      {:date (csv/get-value values 2)
       :amount (amount-to-tappio-amount (csv/get-value values 4))
       :source (csv/get-value values 0)
       :target (csv/get-value values 1)
       :description (clojure.string/trim (clojure.string/replace (csv/get-value values 10)
                                                                 "Viesti:"
                                                                 ""))})))

(defn event-string [index event]
  (str "(event "
       index
       " "
       (date-string-to-tappio-date (:date event))
       " \""
       (:description event)
       "\" "
       (transfer (:source event)
                 (:target event)
                 (:amount event))
       ")\n"))

(defn events-to-string [events]
  (apply str (map event-string
                  (iterate inc 1)
                  events)))

(defn tappio-document [events-string template]
  (clojure.string/replace template "<<events>>" events-string))

(defn create-tappio-document [source target template]
  (-> (read-events source)
      (events-to-string)
      (tappio-document (slurp template))
      ((fn [document] (spit target
                            document
                            :encoding "ISO-8859-15")))))

(defn start [source target template]
  (println "using source:" source)
  (println "using target:" target)
  (println "using template:" template)
  (println "running...")
  (create-tappio-document source target template)
  (println "ready."))

(comment
  (start "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2014/tilinpaatos/tapahtumat_2014_utf.csv"
         "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2014/tilinpaatos/tilikausi_2014.tlk"
         "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2014/tilinpaatos/petkele_account_template_2014.txt")

  (start "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2015/tilinpaatos/tapahtumat_2015_utf.csv"
         "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2015/tilinpaatos/tilikausi_2015.tlk"
         "/Users/jukka/Copy/jukka/petkele/talous/Petkele_talous2015/tilinpaatos/petkele_account_template_2015.txt"))
