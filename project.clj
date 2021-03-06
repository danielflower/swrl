(defproject yswrl "0.1.0-SNAPSHOT"

            :description "You-should-watch-read-listen: recommendations for your friends"
            :url "https://www.swrl.co/"

            :dependencies [[org.slf4j/slf4j-api "1.7.25"]
                           [ch.qos.logback/logback-classic "1.2.3"]
                           [org.slf4j/log4j-over-slf4j "1.7.25"]
                           [org.clojure/tools.logging "0.3.1"]
                           [org.clojure/clojure "1.8.0"]
                           [org.clojure/data.json "0.2.6"]
                           [ring-server "0.4.0"]
                           [selmer "1.10.7"]
                           [clj-time "0.13.0"]
                           [clj-http "3.5.0"]
                           [environ "1.1.0"]
                           [compojure "1.5.2"]
                           [ring/ring-defaults "0.2.3"]
                           [ring-middleware-format "0.7.2"]
                           [ring-json-params "0.1.3"]
                           [amalloy/ring-gzip-middleware "0.1.3"]
                           [noir-exception "0.2.5"]
                           [korma "0.4.2"]
                           [bouncer "0.3.3"]
                           [prone "1.1.4"]
                           [buddy "0.5.2"]
                           [ragtime "0.5.2"]
                           [org.clojure/data.zip "0.1.2"]
                           [org.clojure/data.xml "0.0.8"]
                           [org.postgresql/postgresql "9.3-1102-jdbc41"]
                           [clj-oauth2 "0.2.0"]
                           [enlive "1.1.5"]
                           [clj-json "0.5.3"]]

            :min-lein-version "2.0.0"
            :uberjar-name "yswrl.jar"
            :repl-options {:init-ns yswrl.handler}
            :jvm-opts ["-server"]


            :plugins [[lein-ring "0.9.1"]
                      [lein-environ "1.0.0"]
                      [lein-ancient "0.6.5"]
                      [lein-midje "3.1.3"]]

            :ring {:handler      yswrl.handler/app
                   :init         yswrl.handler/init
                   :destroy      yswrl.handler/destroy
                   :uberwar-name "yswrl.war"}

            :profiles {:uberjar        {:omit-source false
                                        :env         {:production "true"}
                                        :main        yswrl.core
                                        :aot         :all}
                       :dev            {:dependencies [[ring-mock "0.1.5"]
                                                       [ring/ring-devel "1.5.1"]
                                                       [clj-http-fake "1.0.3"]
                                                       [kerodon "0.6.1"]
                                                       [midje "1.8.2" :exclusions [org.clojure/clojure]]]
                                        :source-paths ["env/dev/clj"]
                                        :main         yswrl.core

                                        :repl-options {:init-ns yswrl.repl}
                                        :env          {:dev "true"}}
                       :scheduled-jobs {:omit-source true
                                        :main        yswrl.jobs
                                        :env         {:production "true"}
                                        :aot         :all}}


            :aliases {"run-jobs" ["with-profile" "scheduled-jobs" "run"]})

