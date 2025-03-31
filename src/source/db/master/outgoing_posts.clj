(ns source.db.master.outgoing-posts
  (:require [source.db.master.connection :refer [ds] :as c]
            [hugsql.core :as hugsql]
            [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-outgoing-posts-table)
(declare select-outgoing-posts-by-bundle-id)
(declare insert-outgoing-post)
(declare select-outgoing-posts-by-category)
(declare count-outgoing-posts)
(declare count-outgoing-posts-by-content-type)

(hugsql/def-db-fns "source/db/master/sql/outgoing_posts.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})

(comment
  (create-outgoing-posts-table ds)
  (select-outgoing-posts-by-category ds {:bundle-id 1 :content-type "video"})
  (time
  (count-outgoing-posts-by-content-type ds {:bundle-id 1 :content-type "video"})
   ))