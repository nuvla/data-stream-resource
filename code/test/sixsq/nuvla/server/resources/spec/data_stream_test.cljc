(ns sixsq.nuvla.server.resources.spec.data-stream-test
    (:require
    [clojure.test :refer [deftest]]
    [sixsq.nuvla.server.resources.data-stream :as data-stream-resource]
    [sixsq.nuvla.server.resources.spec.data-stream :as data-stream]
    [sixsq.nuvla.server.resources.spec.spec-test-utils :as stu]))


(def valid-acl {:owners   ["group/nuvla-admin"]
                :view-acl ["group/nuvla-anon"]})


(deftest check-data-stream
  (let [timestamp   "1972-10-08T10:00:00.00Z"

        data-stream {:id                     (str data-stream-resource/resource-type "/uuid")
                     :resource-type          data-stream-resource/resource-type
                     :created                timestamp
                     :updated                timestamp
                     :acl                    valid-acl

                     ;; mandatory
                     :name                   "foo data"
                     :device                 "nuvlabox/8c07d5d0-3d86-11ec-8b86-60f81dcabcfa"
                     :data                   "some data"
                     :bytes                  10234
                     :content-type           "text/html; charset=utf-8"

                     ;; optional
                     :description            "bar data"
                     :timestamp              timestamp
                     :location               [6.143158 46.204391 373.0]
                     :md5sum                 "abcde"

                     :other                  "foo"}]

    (stu/is-valid ::data-stream/schema data-stream)

    ;; mandatory keywords
    (doseq [k #{:created :updated :acl :name :data :device :bytes :content-type}]
      (stu/is-invalid ::data-stream/schema (dissoc data-stream k)))

    ;; optional keywords
    (doseq [k #{:other :md5sum :timestamp :location }]
      (stu/is-valid ::data-stream/schema (dissoc data-stream k)))))
