(ns petkele-account.main
  (:require [petkele-account.core :as core])
  (:gen-class))

(defn -main [& args]
  (apply core/start args))










