(ns sandbox.records
  (:require [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers]
            [jdbc.core :as jdbc]))

(def db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})

(defn where-id
  ( [id]
   (helpers/where [:= :id id]))
  ( [map id]
   (helpers/where map [:= :id id])))

(defn get-where [model-type-key predicate-triples]
  "predicate triples a vector of vectors e.g [[:= :id model-id]]"
  (let [table model-type-key
        triples predicate-triples
        sqlvec (-> (apply where triples)
                   (select :*)
                   (from table)
                   sql/format)
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
    (jdbc/execute connection sqlvec)))

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