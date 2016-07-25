(ns capture-me.app
  (:require [cljsjs.leaflet-locatecontrol]
            [cljsjs.react-leaflet]
            [rum.core :as rum]
            [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

(def tilelayer-options
  {:url         "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}"
   :maxZoom     18
   :minZoom     10
   :id          "mapbox.streets"
   :accessToken "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw"
   :attribution "© <a href='https://www.mapbox.com/map-feedback/'>Mapbox</a> © <a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})

(rum/defc pokemap
  []
  (js/React.createElement js/ReactLeaflet.Map (clj->js {:zoom 13 :center [35.0853 -106.6056]})
                          (js/React.createElement js/ReactLeaflet.TileLayer (clj->js tilelayer-options))
                          (js/React.createElement js/ReactLeaflet.Marker (clj->js {:position [35.08 -106.6]})
                                                  (js/React.createElement js/ReactLeaflet.Popup #js {} (html [:span "Albuquerque"])))))

(defn init []
  (rum/mount
    (pokemap)
    (. js/document (getElementById "pokemap"))))
