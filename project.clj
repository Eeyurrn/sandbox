(defproject sandbox "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha16"]
                 [bidi "2.1.2"]
                 [honeysql "0.9.1"]
                 [org.clojure/java.jdbc "0.7.3"]
                 [funcool/clojure.jdbc "0.9.0"]
                 [org.xerial/sqlite-jdbc "3.21.0"]]
  :main ^:skip-aot sandbox.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
