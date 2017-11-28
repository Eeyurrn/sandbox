(ns sandbox.records
  (:require [honeysql.core :as sql]
            [honeysql.helpers :refer :all :as helpers]
            [jdbc.core :as jdbc]
            [clojure.java.jdbc :refer [create-table-ddl]]))

(def db-spec
  {:classname   "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname     "db/database.db"})


(def create-user-table (create-table-ddl :users
                                       [[:id :int "PRIMARY KEY"]
                                        [:name :text]
                                        [:member :int]]
                                       {:conditional? true}))

(def create-pet-table (create-table-ddl :pets
                                        [[:id :int]
                                         [:user_id :int]
                                         [:name :text]
                                         [:favourite_food :text]]))

(defn execute-query! [query]
  (jdbc/execute (jdbc/connection db-spec) query))

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

(defn create-model! [model-type-key model-attrs-map]
  (let [table model-type-key
        attrs model-attrs-map
        conn (jdbc/connection db-spec)
        sqlvec (-> (insert-into table)
                   (values [attrs])
                   sql/format)]
    (jdbc/execute conn sqlvec)))

(defn update-model! [model-type-key model-id update-attrs]
  (let [table model-type-key
        id model-id
        attrs update-attrs
        sqlvec (-> (helpers/update table)
                   (sset attrs)
                   (where [:= :id id])
                   sql/format)
        conn (jdbc/connection db-spec)]
    (jdbc/execute conn sqlvec)))

(defn delete-model! [model-type-key model-id]
  (let [table model-type-key
        id model-id
        sqlvec (-> (delete-from table)
                   (where [:= :id id])
                   sql/format)
        conn (jdbc/connection db-spec)]
    (jdbc/execute conn sqlvec)))
