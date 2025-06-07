(ns source.cache
  (:require [source.util :as util]))


(defprotocol Cache
  (get-item [this uuid])
  (add-item [this item])
  (add-item-with-uuid [this item uuid])
  (remove-item [this uuid])
  (get-all-items [this]))


(defn- -get-item [*cache uuid]
  (get-in @*cache [uuid]))


(defn- -remove-item [*cache uuid]
  (swap! *cache dissoc uuid))


(defn- -add-item [*cache item & {:keys [uuid]}]
  (let [uuid (or uuid (util/uuid))]
    (swap! *cache assoc uuid item)
    {:item item
     :uuid uuid}))


(defn -get-all-items [*cache]
  @*cache)


(defn create-cache []
  (let [lcache (atom {})]
    (reify Cache
      (get-item [_ uuid]
        (-get-item lcache uuid))
      (remove-item [_ uuid]
        (-remove-item lcache uuid))
      (add-item [_ item]
        (-add-item lcache item))
      (add-item-with-uuid [_ item uuid]
        (-add-item lcache item :uuid uuid))
      (get-all-items [_]
        (-get-all-items lcache)))))


(comment
  (def cache (create-cache))
  (add-item-with-uuid cache "testitem" "a1b2c3d4e5f6")
  (add-item-with-uuid cache "anothertestitem" "cheese")
  (add-item cache "yetanothertestitem")
  (get-item cache "a1b2c3d4e5f6")
  (remove-item cache "a1b2c3d4e5f6")
  (get-all-items cache))

