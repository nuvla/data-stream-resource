(ns sixsq.nuvla.server.resources.data-stream
    "
  The `data-stream` resource is the data and meta-data ingestion resource.

  The schema for the this resource is open, allowing any information to be
  associated with the data object. The only requirement is that keys must be
  prefixed. The prefixes **must** be defined in a `data-record-key-prefix`
  resource and the key itself **may** be described in a `data-record-key`
  resource.
  "
    (:require
    [ring.util.response :as r]
    [sixsq.nuvla.auth.acl-resource :as a]
    [sixsq.nuvla.auth.utils :as auth]
    [sixsq.nuvla.server.resources.common.crud :as crud]
    [sixsq.nuvla.server.resources.common.std-crud :as std-crud]
    [sixsq.nuvla.server.resources.common.utils :as u]
    [sixsq.nuvla.server.resources.data-record-key-prefix :as sn]
    [sixsq.nuvla.server.resources.resource-metadata :as md]
    [sixsq.nuvla.server.resources.spec.acl-collection :as acl-collection]
    [sixsq.nuvla.server.resources.spec.data-stream :as data-stream]
    [sixsq.nuvla.server.util.kafka :as ka]
    [sixsq.nuvla.server.util.metadata :as gen-md]
    [sixsq.nuvla.server.util.response :as sr]))


(def ^:const resource-type (u/ns->type *ns*))


(def ^:const collection-type (u/ns->collection-type *ns*))


(def collection-acl {:add         ["group/nuvla-user"]})


;;
;; multimethods for validation and operations
;;

(defn key-prefix
  "Extracts the key's prefix if there is one. Returns nil otherwise."
  [k]
  (some->> k
           name
           (re-matches #"(.+):.*")
           second))


(defn valid-key-prefix?
  "If there is a prefix and it is NOT in the valid-prefixes set, return false.
   Otherwise return true."
  [valid-prefixes k]
  (if-let [prefix (key-prefix k)]
    (boolean (valid-prefixes prefix))
    true))


(defn- valid-attributes?
  [validator resource]
  (if-not (map? resource)
    true
    (and (every? validator (keys resource))
         (every? (partial valid-attributes? validator) (vals resource)))))


(defn- throw-wrong-namespace
  []
  (let [code     406
        msg      "resource uses keys with undefined prefixes"
        response (-> {:status code :message msg}
                     sr/json-response
                     (r/status code))]
    (throw (ex-info msg response))))


(defn- validate-attributes
  [resource]
  (let [valid-prefixes (sn/all-prefixes)
        validator      (partial valid-key-prefix? valid-prefixes)]
    (if (valid-attributes? validator resource)
      resource
      (throw-wrong-namespace))))


(def validate-fn (u/create-spec-validation-fn ::data-stream/schema))
(defmethod crud/validate resource-type
  [resource]
  (-> resource
      validate-fn
      validate-attributes))


;;
;; multimethod for ACLs
;;

(defn create-acl [id]
  {:owners   ["group/nuvla-admin"]
   :edit-acl [id]})


(defmethod crud/add-acl resource-type
  [resource request]
  (a/add-acl resource request))


(defmethod crud/add-acl resource-type
  [{:keys [acl] :as resource} request]
  (if acl
    resource
    (let [active-claim (auth/current-active-claim request)]
      (assoc resource :acl (create-acl active-claim)))))


;;
;; CRUD operations
;;

(def validate-collection-acl (u/create-spec-validation-fn ::acl-collection/acl))


(defn add-fn
  [resource-name collection-acl resource-uri]
  (validate-collection-acl collection-acl)
  (fn [{:keys [body] :as request}]
    (a/throw-cannot-add collection-acl request)
    (let [id (u/new-resource-id resource-name)]
      (ka/publish-async
        resource-name
        id
        (-> body
            u/strip-service-attrs
            (assoc :id id)
            (assoc :resource-type resource-uri)
            u/update-timestamps
            (u/set-created-by request)
            (crud/add-acl request)
            crud/validate))
      (sr/response-created id))))


(def add-impl (add-fn resource-type collection-acl resource-type))

(defmethod crud/add resource-type
  [request]
  (add-impl request))


(defn- throw-method-not-allowed
  []
  (let [code     405
        msg      "method not allowed"
        response (-> {:status code :message msg}
                     sr/json-response
                     (r/status code))]
    (throw (ex-info msg response))))


(defmethod crud/retrieve resource-type
  [_]
  (throw-method-not-allowed))


(defmethod crud/edit resource-type
  [_]
  (throw-method-not-allowed))


(defmethod crud/delete resource-type
  [_]
  (throw-method-not-allowed))


(defmethod crud/query resource-type
  [_]
  (throw-method-not-allowed))

;;
;; initialization
;;

(def resource-metadata (gen-md/generate-metadata ::ns ::data-stream/schema))


(defn initialize
  []
  (std-crud/initialize resource-type ::data-stream/schema)
  (md/register resource-metadata))

