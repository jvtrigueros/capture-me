(ns capture-me.app
  (:require [cljsjs.leaflet-locatecontrol]
            [cljsjs.react-leaflet]
            [rum.core :as rum]
            [sablono.core :refer-macros [html]]))

(enable-console-print!)

(def app-state (atom {:position [35.0853 -106.6056]}))

(def tilelayer-options
  {:url         "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}"
   :maxZoom     18
   :minZoom     10
   :id          "mapbox.streets"
   :accessToken "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw"
   :attribution "© <a href='https://www.mapbox.com/map-feedback/'>Mapbox</a> © <a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})

(defn pokemon-icon [idx]
  (js/L.divIcon #js {:className (str "pokemon pokemon-" idx)
                     :iconSize  #js [40 32]}))

(def ds-connector-mixin
  {:init         (fn [state props]
                   (assoc state ::client (js/deepstream "localhost:6020")))
   :will-mount   (fn [state]
                   (let [client (::client state)]
                     (.login client)
                     state))
   :did-mount    (fn [state]
                   (let [client (::client state)
                         record (.getRecord (.-record client) "pokemon/1")]
                     (.subscribe record #(.log js/console %))
                     state))
   :will-unmount (fn [state]
                   (let [client (::client state)]
                     (println "unmount")
                     (.close client)
                     state))})

(rum/defc ds-connector < ds-connector-mixin
  []
  [:div])

(rum/defc pokemap < rum/reactive
  []
  (let [state (rum/react app-state)
        position (:position state)]
    (js/React.createElement js/ReactLeaflet.Map (clj->js {:zoom 13 :center position})
                            (js/React.createElement js/ReactLeaflet.TileLayer (clj->js tilelayer-options))
                            (js/React.createElement js/ReactLeaflet.Marker (clj->js {:position position
                                                                                     :icon     (pokemon-icon 1)})
                                                    (js/React.createElement js/ReactLeaflet.Popup #js {} (html [:span "Albuquerque"]))))))

(rum/defc sighted-button
  "Toggles the map."
  [show-map]
  [:button.spot-button {:on-click (fn [_] (do
                                            (swap! show-map not)))}
   "Sighted!"])

(rum/defcs app < (rum/local {::show-map true})
  [state]
  (let [show-map (rum/cursor (:rum/local state) ::show-map)]

    [:div

     (if @show-map (pokemap))
     #_(sighted-button show-map)
     #_(ds-connector)]))

(defn init []
  (rum/mount
    (app)
    (. js/document (getElementById "pokemap"))))
