(ns source.services-new.sectors
  (:require [source.services-new.schemas :as schemas]))

;; Service schemas
(def GetSectors
  [:vector schemas/Sector])

(def GetSector 
  schemas/Sector)

(def CreateSectors 
  [:vector schemas/Sector])
