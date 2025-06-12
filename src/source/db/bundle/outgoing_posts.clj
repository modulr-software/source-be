(ns source.db.bundle.outgoing-posts
  (:require
   [hugsql.core :as hugsql]
   [hugsql.adapter.next-jdbc :as next-adapter]))

(declare create-table!)
(declare select-outgoing-posts-by-bundle-id)
(declare insert-outgoing-post!)
(declare select-outgoing-posts-by-category)
(declare count-outgoing-posts)
(declare count-outgoing-posts-by-content-type)
(declare select-outgoing-post-by-id)
(declare drop-table!)
(hugsql/def-db-fns "source/db/bundle/sql/outgoing_posts.sql"
  {:adapter (next-adapter/hugsql-adapter-next-jdbc)})
