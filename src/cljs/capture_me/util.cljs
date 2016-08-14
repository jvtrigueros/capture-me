(ns capture-me.util
  (:require [clojure.string :as str]))

(defn ->kebab-case-string
  [string]
  (-> string
      str/lower-case
      (str/replace " " "-")))
