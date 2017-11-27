(ns sandbox.core
  (:gen-class)
  (:require [clojure.spec.alpha :as s]
            [clojure.set :as set]
            [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers]
            [jdbc.core :as jdbc])

  (:use [bidi.bidi]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def countries-str (slurp "countries.txt"))
(def all-countries (map (fn [line]
                          (let [[full-name _ three-letter] (clojure.string/split line #"\t")]
                            [full-name three-letter]))
                        (clojure.string/split-lines countries-str)))

(def products #{:t-shirt :sticker :throw-pillow :mug :pencil-skirt
                :tapestry :classic-tee :hoodie :poster :laptop-skin
                :ipad-case :iphone-case :clock :spiral-notebook :tote-bad
                :studio-pouch :hardcover-journal})

(def all-three-letter-iso (into #{} (map second all-countries)))

(def rh-rule {:membership :exclude
              :by         :country
              :rules      [[#{"USA"} #{:t-shirt}]
                           [#{"DEU"} #{:mug :t-shirt}]]})

(defn invert-country-products [[country-set product-set]]
  [(set/difference all-three-letter-iso country-set) (set/difference products product-set)])

(defn countries-by-product [rule]
  (let [{rules :rules} rule
        product-key :t-shirt]
    (->> rules
         (filter (fn [[countries products]] (contains? products product-key)))
         (map first))))

(defn invert-rule [{membership :membership
                    rules      :rules}]
  {:rules      (vec (map invert-country-products rules))
   :membership (if (= :exclude membership)
                 :include
                 :exclude)})

(def db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn where-id
  ( [id]
    (helpers/where [:= :id id]))
  ( [map id]
    (helpers/where map [:= :id id])))

(defn select-*
  "docstring"
  [model-type-key predicate-triples]
  (let [trips predicate-triples]
    (-> (apply where (or (empty? trips) [nil] trips))
        (select :*)
        (from model-type-key)
        sql/format)))


(defn get-where [model-type-key predicate-triples]
  "predicate triples a vector of vectors e.g [[:= :id model-id]]"
  (let [sqlvec (select-* model-type-key predicate-triples)
        conn (jdbc/connection db-spec)]
    (jdbc/fetch conn sqlvec)))

(defn get-model [model-type-key model-id]
  (get-where model-type-key [[:= :id model-id]]))

(defn create-model [model-type-key model-attrs-map]
  (let [table model-type-key
        attrs model-attrs-map
        conn (jdbc/connection db-spec)
        sqlvec (-> (insert-into table)
                   (values [attrs])
                   sql/format)]
    (jdbc/execute conn sqlvec)))

(defn update-model [model-type-key model-id update-attrs]
  (let [table model-type-key
        id model-id
        attrs update-attrs
        sqlvec (-> (helpers/update table)
                   (sset attrs)
                   (where [:= :id id])
                   sql/format)
        conn (jdbc/connection db-spec)]
    (jdbc/execute conn sqlvec)))

(defn delete-model [model-type-key model-id]
  (let [table model-type-key
        id model-id
        sqlvec (-> (delete-from table)
                   (where [:= :id id])
                   sql/format)
        conn (jdbc/connection db-spec)]
    (jdbc/execute conn sqlvec)))