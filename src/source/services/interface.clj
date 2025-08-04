(ns source.services.interface
  (:require  [source.services.users :as users]
             [source.db.interface :as db]
             [source.services.auth :as auth]
             [source.services.xml-schemas :as xml]
             [source.services.bundles :as bundles]))

(defn users
  [& args]
  (apply users/users args))

(defn user [ds {:keys [_id] :as opts}]
  (users/user ds opts))

(defn insert-user! [ds {:keys [_values _ret] :as opts}]
  (users/insert-user! ds opts))

(defn update-user! [ds {:keys [_id _values _where] :as opts}]
  (users/update-user! ds opts))

(defn login [ds {:keys [_email] :as opts}]
  (auth/login ds opts))

(defn register [ds user]
  (auth/register ds user))

(defn add-selection-schema! [store db {:keys [_schema _record] :as opts}]
  (xml/add-selection-schema! store db opts))

(defn selection-schema [ds {:keys [_id] :as opts}]
  (xml/selection-schema ds opts))

(defn bundle [ds {:keys [_id _where] :as opts}]
  (bundles/bundle ds opts))

(defn selection-schemas [ds]
  (xml/selection-schemas ds))

(defn selection-schemas-by-provider [ds {:keys [_provider-id] :as opts}]
  (xml/selection-schemas-by-provider ds opts))

(defn ast [url]
  (xml/ast url))

(defn extract-data [store {:keys [schema-id url]}]
  (xml/extract-data store schema-id url))

(defn output-schemas [store]
  (xml/output-schemas store))

(defn output-schema [store output-schema-id]
  (xml/output-schema store output-schema-id))

(defn add-output-schema! [store schema]
  (xml/add-output-schema! store schema))

(defn providers [store]
  (xml/providers store))

(defn provider [store provider-id]
  (xml/provider store provider-id))

(defn delete-provider! [store provider-id]
  (xml/delete-provider! store provider-id))

(defn add-provider! [store name]
  (xml/add-provider! store name))

(comment
  (users (db/ds :master))
  (user (db/ds :master) {:id 2})
  (login (db/ds :master) {:email "merveillevaneck@gmail.com"})
  ())
