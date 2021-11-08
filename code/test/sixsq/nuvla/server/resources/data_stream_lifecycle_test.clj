(ns sixsq.nuvla.server.resources.data-stream-lifecycle-test
  (:require
    [clojure.data.json :as json]
    [clojure.test :refer [deftest is join-fixtures use-fixtures]]
    [peridot.core :refer [content-type header request session]]
    [sixsq.nuvla.server.app.params :as p]
    [sixsq.nuvla.server.middleware.authn-info :refer [authn-info-header]]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.data-stream :as t]
    [sixsq.nuvla.server.resources.data-record-key-prefix :as sn]
    [sixsq.nuvla.server.resources.lifecycle-test-utils :as ltu]))


(def base-uri (str p/service-context t/resource-type))


(def ns1-prefix (ltu/random-string "ns1-"))


(def ns2-prefix (ltu/random-string "ns2-"))


(def invalid-prefix (ltu/random-string))


(def core-attrs {
                 ;; mandatory
                 :name         "text"
                 :device       "nuvlabox/8c07d5d0-3d86-11ec-8b86-60f81dcabcfa"
                 :data         "foo data"
                 :bytes        8
                 :content-type "plain/text"

                 ;; optional
                 :description  "bar data"
                 :timestamp    "2019-04-15T12:23:53.00Z"
                 :location     [6.143158 46.204391 373.0]
                 :md5sum       "abcde"})


(def ns1 {:prefix ns1-prefix
          :uri    (str "https://example.org/" ns1-prefix)})


(def ns2 {:prefix ns2-prefix
          :uri    (str "https://example.org/" ns2-prefix)})


(def valid-entry
  (merge core-attrs
         {(keyword (str ns1-prefix ":att1")) "123.456"}))


(def invalid-entry
  {:other "invalid"})


(def entry-wrong-namespace
  (assoc valid-entry (keyword (str invalid-prefix ":att1")) "123.456"))


(defn create-service-attribute-namespaces-fixture
  [f]
  (let [session-admin (-> (session (ltu/ring-app))
                          (content-type "application/json")
                          (header authn-info-header "group/nuvla-admin group/nuvla-admin group/nuvla-user group/nuvla-anon"))]

    (doseq [namespace [ns1 ns2]]
      (-> session-admin
          (request (str p/service-context sn/resource-type)
                   :request-method :post
                   :body (json/write-str namespace))
          (ltu/body->edn)
          (ltu/is-status 201))))
  (f))


(use-fixtures :once (join-fixtures [ltu/with-test-server-kafka-fixture create-service-attribute-namespaces-fixture]))


(deftest lifecycle

  (let [session-anon  (-> (session (ltu/ring-app))
                          (content-type "application/json"))
        session-admin (header session-anon authn-info-header
                              "group/nuvla-admin group/nuvla-admin group/nuvla-user group/nuvla-anon")
        session-user  (header session-anon authn-info-header "user/jane user/jane group/nuvla-user group/nuvla-anon")]

    ;; anonymous create should fail
    (-> session-anon
        (request base-uri
                 :request-method :post
                 :body (json/write-str valid-entry))
        (ltu/body->edn)
        (ltu/is-status 403))

    ;; creation rejected because attribute belongs to unknown namespace
    (-> session-user
        (request base-uri
                 :request-method :post
                 :body (json/write-str entry-wrong-namespace))
        (ltu/is-status 406))

    ;; adding entry as user should succeed
    (-> session-user
        (request base-uri
                 :request-method :post
                 :body (json/write-str valid-entry))
        (ltu/body->edn)
        (ltu/is-status 201)
        (ltu/location))

    ;; try adding invalid entry
    (-> session-admin
        (request base-uri
                 :request-method :post
                 :body (json/write-str invalid-entry))
        (ltu/body->edn)
        (ltu/is-status 400))

  ;; retrieving, deleting and querying is not possible
    (let [uri     (-> session-user
                      (request base-uri
                               :request-method :post
                               :body (json/write-str valid-entry))
                      (ltu/body->edn)
                      (ltu/is-status 201)
                      (ltu/location))
          abs-uri (str p/service-context uri)]

      (-> session-admin
          (request abs-uri)
          (ltu/body->edn)
          (ltu/is-status 405))

      (-> session-admin
          (request abs-uri
                   :request-method :delete)
          (ltu/body->edn)
          (ltu/is-status 405))
      )

    (-> session-admin
       (request (str base-uri "?filter=foo='bar'")
                :request-method :put)
       (ltu/body->edn)
       (ltu/is-status 405)
       (ltu/body))
    ))


(deftest bad-methods
  (let [resource-uri (str p/service-context (u/new-resource-id t/resource-type))]
    (ltu/verify-405-status [[base-uri :options]
                            [resource-uri :options]
                            [resource-uri :post]])))
