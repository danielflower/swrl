(defproject yswrl "0.1.0-SNAPSHOT"

            :description "You-should-watch-read-listen: recommendations for your friends"
            :url "http://www.swrl.co/"

            :dependencies [[org.slf4j/slf4j-api "1.7.12"]
                           [ch.qos.logback/logback-classic "1.1.3"]
                           [org.slf4j/log4j-over-slf4j "1.7.12"]
                           [org.clojure/tools.logging "0.3.1"]
                           [org.clojure/clojure "1.7.0"]
                           [ring-server "0.4.0"]
                           [selmer "0.8.5"]
                           [clj-time "0.9.0"]
                           [clj-mandrill "0.1.0"]
                           [clj-http "1.1.2"]
                           [environ "1.0.0"]
                           [compojure "1.3.4"]
                           [ring/ring-defaults "0.1.5"]
                           [ring-middleware-format "0.5.0"]
                           [noir-exception "0.2.5"]
                           [korma "0.4.2"]
                           [bouncer "0.3.3"]
                           [prone "0.8.2"]
                           [buddy "0.5.2"]
                           [ragtime "0.3.8"]
                           [org.clojure/data.zip "0.1.1"]
                           [org.clojure/data.xml "0.0.8"]
                           [org.postgresql/postgresql "9.3-1102-jdbc41"]
                           [clj-oauth2 "0.2.0"]
                           [enlive "1.1.5"]]

            :min-lein-version "2.0.0"
            :uberjar-name "yswrl.jar"
            :repl-options {:init-ns yswrl.handler}
            :jvm-opts ["-server"]


            :plugins [[lein-ring "0.9.1"]
                      [lein-environ "1.0.0"]
                      [lein-ancient "0.6.5"]
                      [com.jakemccrary/lein-test-refresh "0.9.0"]
                      [ragtime/ragtime.lein "0.3.8"]
                      [lein-midje "3.1.3"]]

            :test-refresh {:quiet true}

            :ring {:handler      yswrl.handler/app
                   :init         yswrl.handler/init
                   :destroy      yswrl.handler/destroy
                   :uberwar-name "yswrl.war"}

            :ragtime
            {:migrations ragtime.sql.files/migrations
             :database   (or (System/getenv "JDBC_DATABASE_URL") "jdbc:postgresql://localhost/yswrl?user=dev&password=password")}



            :profiles {:uberjar        {:omit-source true
                                        :env         {:production true}
                                        :main        yswrl.core
                                        :aot         :all}
                       :dev            {:dependencies [[ring-mock "0.1.5"]
                                                       [ring/ring-devel "1.3.2"]
                                                       [clj-http-fake "1.0.1"]
                                                       [pjstadig/humane-test-output "0.7.0"]
                                                       [kerodon "0.6.1"]
                                                       [midje "1.8.2" :exclusions [org.clojure/clojure]]]
                                        :source-paths ["env/dev/clj"]
                                        :main         yswrl.core


                                        :repl-options {:init-ns yswrl.repl}
                                        :injections   [(require 'pjstadig.humane-test-output)
                                                       (pjstadig.humane-test-output/activate!)]
                                        :env          {:dev true}}
                       :scheduled-jobs {:omit-source true
                                        :main        yswrl.jobs
                                        :env         {:production true}
                                        :aot         :all}
                       }

            :aliases {"run-jobs" ["with-profile" "scheduled-jobs" "run"]}

            )