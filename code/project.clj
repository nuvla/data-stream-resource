(def parent-version "6.7.5")
(def nuvla-ring-version "2.0.2")
(def nuvla-api-server-version "5.18.1-SNAPSHOT")


(defproject sixsq.nuvla.server/data-stream-jar "0.0.1-SNAPSHOT"

  :description "data stream resource"

  :url "https://github.com/nuvla/data-stream"

  :license {:name         "Apache 2.0"
            :url          "https://sixsq.com"
            :distribution :repo}

  :plugins [[lein-parent "0.3.5"]
            [lein-environ "1.1.0"]]

  :parent-project {:coords  [sixsq.nuvla/parent ~parent-version]
                   :inherit [:plugins
                             :min-lein-version
                             :managed-dependencies
                             :repositories
                             :deploy-repositories]}

  :source-paths ["src"]

  :resource-paths ["resources"]

  :pom-location "target/"

  :profiles
  {:provided {:dependencies [[org.clojure/clojure]
                             [sixsq.nuvla.ring/code ~nuvla-ring-version]
                             [sixsq.nuvla.server/api-jar ~nuvla-api-server-version]]}
   :test     {:dependencies   [[me.raynes/fs]
                               [peridot]
                               [org.apache.logging.log4j/log4j-core] ;; needed for ES logging
                               [org.apache.logging.log4j/log4j-api] ;; needed for ES logging
                               [org.clojure/test.check]
                               [org.elasticsearch.client/transport]
                               [org.elasticsearch.test/framework]
                               [org.slf4j/slf4j-api]
                               [org.slf4j/slf4j-log4j12]
                               [com.cemerick/url]
                               [org.apache.curator/curator-test]
                               [sixsq.nuvla.server/api-test-jar ~nuvla-api-server-version]]
              :resource-paths ["test-resources"]
              :env            {:nuvla-session-key "test-resources/session.key"
                               :nuvla-session-crt "test-resources/session.crt"}
              :aot            :all}
   :dev      {:resource-paths ["test-resources"]
              :dependencies [;; for kafka embedded
                             [org.apache.kafka/kafka-clients "2.4.0"]
                             [org.apache.kafka/kafka_2.12 "2.4.0"]
                             [org.apache.zookeeper/zookeeper "3.5.6"
                              :exclusions [io.netty/netty
                                           jline
                                           org.apache.yetus/audience-annotations
                                           org.slf4j/slf4j-log4j12
                                           log4j]]]}})
