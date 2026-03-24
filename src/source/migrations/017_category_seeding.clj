(ns source.migrations.017-category-seeding
  (:require [source.db.master]
            [source.db.honey :as hon]
            [source.workers.categories :as categories]))

(defn run-up! [context]
  (let [ds-master (:db-master context)]
    (run!
     #(categories/delete-category! ds-master (:id %))
     (hon/find ds-master {:tname :categories}))

    (hon/insert! ds-master {:tname :categories
                            :data [{:name "Regenerative finance and investment"}
                                   {:name "Regenerative business"}
                                   {:name "Sustainable fashion and textiles"}
                                   {:name "Regenerative agriculture and food"}
                                   {:name "Energy and clean technology"}
                                   {:name "Circularity and waste"}
                                   {:name "Built environment"}
                                   {:name "Transport and mobility"}
                                   {:name "Travel and regenerative tourism"}
                                   {:name "Technology and AI for good"}
                                   {:name "Wellbeing and mental health"}
                                   {:name "Social justice and equity"}
                                   {:name "Indigenous wisdom and land rights"}
                                   {:name "Leadership and inner development"}
                                   {:name "Policy and systems change"}
                                   {:name "Water and freshwater systems"}
                                   {:name "Biodiversity and conservation"}
                                   {:name "Ocean and marine"}
                                   {:name "Bioregionalism and local economies"}
                                   {:name "Community and culture"}
                                   {:name "Arts and artivism"}
                                   {:name "Education and climate literacy"}
                                   {:name "Media and communications"}
                                   {:name "Nonprofits and philanthropy"}
                                   {:name "Consumer goods and conscious living"}
                                   {:name "Spirituality and consciousness"}
                                   {:name "Gender and women's leadership"}
                                   {:name "Health and planetary health"}]})))

(defn run-down! [context]
  (let [ds-master (:db-master context)]
    (run!
     #(categories/delete-category! ds-master (:id %))
     (hon/find ds-master {:tname :categories}))

    (hon/insert! ds-master {:tname :categories
                            :data [{:name "programming"}
                                   {:name "game development"}
                                   {:name "languages"}
                                   {:name "technology"}]})))
