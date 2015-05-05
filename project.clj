(defproject yswrl "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[org.slf4j/slf4j-api "1.7.12"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [org.slf4j/log4j-over-slf4j "1.7.12"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/clojure "1.6.0"]
                 [ring-server "0.4.0"]
                 [selmer "0.8.2"]
                 [clj-time "0.8.0"]
                 [clj-mandrill "0.1.0"]
                 [environ "1.0.0"]
                 [im.chit/cronj "1.4.3"]
                 [compojure "1.3.3"]
                 [ring/ring-defaults "0.1.4"]
                 [ring-middleware-format "0.5.0"]
                 [noir-exception "0.2.3"]
                 [korma "0.4.0"]
                 [bouncer "0.3.2"]
                 [prone "0.8.1"]
                 [buddy "0.5.1"]
                 [ragtime "0.3.8"]
                 [org.postgresql/postgresql "9.3-1102-jdbc41"]]

  :min-lein-version "2.0.0"
  :uberjar-name "yswrl.jar"
  :repl-options {:init-ns yswrl.handler}
  :jvm-opts ["-server"]

  :main yswrl.core

  :plugins [[lein-ring "0.9.1"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.6.5"]
            [com.jakemccrary/lein-test-refresh "0.9.0"]
            [ragtime/ragtime.lein "0.3.8"]]




  :ring {:handler yswrl.handler/app
         :init    yswrl.handler/init
         :destroy yswrl.handler/destroy
         :uberwar-name "yswrl.war"}

  :ragtime
  {:migrations ragtime.sql.files/migrations
   :database (or (System/getenv "JDBC_DATABASE_URL") "jdbc:postgresql://localhost/yswrl?user=dev&password=password")}



  :profiles
  {:uberjar {:omit-source true
             :env {:production true}

             :aot :all}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.7.0"]
                        [kerodon "0.5.0"]]
         :source-paths ["env/dev/clj"]



         :repl-options {:init-ns yswrl.repl}
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true}}})
