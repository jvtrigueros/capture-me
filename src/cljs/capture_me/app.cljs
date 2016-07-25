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

(defn pokemon-icon [idx]
  (js/L.divIcon #js {:className (str "pokemon pokemon-" idx)
                     :iconSize #js [40 32]}))

(rum/defc pokemap < rum/reactive
  []
  (js/React.createElement js/ReactLeaflet.Map (clj->js {:zoom 13 :center (:position (rum/react app-state))})
                          (js/React.createElement js/ReactLeaflet.TileLayer (clj->js tilelayer-options))
                          (js/React.createElement js/ReactLeaflet.Marker (clj->js {:position (:position (rum/react app-state))
                                                                                   :icon (pokemon-icon 1)})
                                                  (js/React.createElement js/ReactLeaflet.Popup #js {} (html [:span "Albuquerque"])))))

(defn init []
  (rum/mount
    (pokemap)
    (. js/document (getElementById "pokemap"))))
