(ns capture-me.app
  (:require [cljsjs.leaflet]
            [cljsjs.leaflet-locatecontrol]
            [rum.core :as rum]))

(rum/defc label [text]
  [:div
   [:h1 "A label"]
   [:p text]])

#_(defn init []
    (rum/mount (label) (. js/document (getElementById "container"))))

(def locate-options
  {:position "topright"
   :strings  {:title "Show me where I am, yo!"}})

(def tilelayer-options
  {:maxZoom     18
   :id          "mapbox.streets"
   :accessToken "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw"
   :attribution "© <a href='https://www.mapbox.com/map-feedback/'>Mapbox</a> © <a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})

(defn init []
  (let [element-name "pokemap"
        mappy (-> js/L
                  (.map element-name)
                  (.setView (array 51 0) 13))]
    (-> js/L
        (.tileLayer "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}"
                    (clj->js tilelayer-options))
        (.addTo mappy))
    (-> js/L
        .-control
        (.locate (clj->js locate-options))
        (.addTo mappy))))


