(ns sixsq.nuvla.server.resources.spec.data-stream
    (:require
    [clojure.spec.alpha :as s]
    [sixsq.nuvla.server.resources.spec.common :as common]
    [sixsq.nuvla.server.resources.spec.core :as core]
    [sixsq.nuvla.server.resources.spec.data :as datas]
    [sixsq.nuvla.server.util.spec :as su]
    [spec-tools.core :as st]))


(def ^:const nuvlabox-id-regex #"^nuvlabox/[a-z0-9]+(-[a-z0-9]+)*(_\d+)?$")


(s/def ::device
  (-> (st/spec (s/and string? #(re-matches nuvlabox-id-regex %)))
      (assoc :name "device"
             :json-schema/type "string"
             :json-schema/display-name "device"
             :json-schema/description "id of device associated with the data stream"
             :json-schema/order 21
             )))


(s/def ::data
  (-> (st/spec ::core/nonblank-string)
      (assoc :name "data"
             :json-schema/type "string"
             :json-schema/display-name "data"
             :json-schema/description "data of the stream"
             :json-schema/order 22)))


(s/def ::schema
  (su/constrained-map keyword? any?
                      common/common-attrs
                      {:req-un [::common/name
                                ::datas/content-type
                                ::datas/bytes
                                ::device
                                ::data]
                       :opt-un [::common/description
                                ::datas/timestamp
                                ::datas/location
                                ::datas/md5sum]}))
