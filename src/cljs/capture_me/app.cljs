(ns capture-me.app
  (:require [cljsjs.react-leaflet]
            [cljsjs.leaflet-locatecontrol]
            [cljsjs.moment]
            [capture-me.util :refer [->kebab-case-string]]
            [rum.core :as rum]
            [sablono.core :refer-macros [html]]))

(enable-console-print!)

(def app-state (atom {:location     [35.0853 -106.6056]
                      :current-time (js/moment)}))

(def tilelayer-options
  {:url         "https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token={accessToken}"
   :maxZoom     18
   :minZoom     10
   :id          "mapbox.high-contrast"
   :accessToken "pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw"
   :attribution "© <a href='https://www.mapbox.com/map-feedback/'>Mapbox</a> © <a href='http://www.openstreetmap.org/copyright'>OpenStreetMap</a>"})

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

(def locate-control-mixin
  {:did-mount (fn [state]
                (let [map (::leaflet-element state)]
                  (-> js/L
                      .-control
                      (.locate #js {:position "topright" :icon "fa fa-location-arrow" :strings #js {:title "Where am I?"}})
                      (.addTo map))
                  state))})

(def crosshairs-mixin
  {:did-mount    (fn [state]
                   (let [map (::leaflet-element state)
                         crosshairs-icon (js/L.divIcon #js {:className "fa fa-crosshairs"})
                         get-center #(.getCenter map)
                         crosshairs (js/L.marker (get-center) #js {:icon crosshairs-icon :clickable false})]
                     (.addTo crosshairs map)
                     (.on map "move" #(.setLatLng crosshairs (get-center)))
                     state))
   :will-unmount (fn [state]
                   (let [map (::leaflet-element state)]
                     (.off map "move")
                     state))})

(defn pokemon-div-icon [idx]
  (js/L.divIcon #js {:className (str "pokemon pokemon-" idx)
                     :iconSize  #js [40 32]}))

(rum/defc pokemap < rum/reactive
                    {:did-mount (fn [state]
                                  (let [map (.getLeafletElement (rum/ref state "poke"))]
                                    (assoc state ::leaflet-element map)))}
                    locate-control-mixin
                    crosshairs-mixin
  []
  (let [state (rum/react app-state)
        position (:location state)]
    (js/React.createElement js/ReactLeaflet.Map (clj->js {:zoom 13 :center position :id "pokemap" :ref "poke"})
                            (js/React.createElement js/ReactLeaflet.TileLayer (clj->js tilelayer-options))
                            (js/React.createElement js/ReactLeaflet.Marker (clj->js {:position position
                                                                                     :icon     (pokemon-div-icon 1)})
                                                    (js/React.createElement js/ReactLeaflet.Popup #js {} (html [:span "Albuquerque"]))))))

(rum/defc timer < rum/reactive
                  {:will-mount   (fn [state]
                                   (let [current-time (rum/cursor app-state :current-time)]
                                     (assoc state ::interval-id (js/setInterval (fn [] (swap! current-time #(.add % 1 "s"))) 1000))))
                   :will-unmount (fn [state]
                                   (do
                                     (js/clearInterval (::interval-id state))
                                     (assoc state ::interval-id nil)))}
  []
  [:p.heading (.format (:current-time (rum/react app-state)) "dddd, MMMM Do YYYY, h:mm:ss a")])

(rum/defc poke-modal < rum/reactive
  [title datalist is-active]
  (let [inputs [[title datalist] ["Location"]]
        kebab #(->kebab-case-string %)
        class (if (rum/react is-active) {:class "is-active"})
        close #(swap! is-active not)
        timestamp (.unix (:current-time @app-state))]
    (println (str "modal " title ":") timestamp)
    [:.modal class
     [:.modal-background {:on-click close}]
     [:.modal-card
      [:.modal-card-head
       [:p.modal-card-title (str "Add " title)]
       [:button.delete {:on-click close}]]
      [:section.modal-card-body
       [:.content
        (map-indexed (fn [idx input]
                       (let [label (first input)
                             id (kebab label)
                             data (second input)]
                         (vector :p.control {:key idx}
                                 [:label.label {:for id} label]
                                 [:input.input {:name id
                                                :list id
                                                :type "text"}]
                                 [:datalist {:id id}
                                  (map-indexed #(vector :option {:value %2 :key %1}) data)])))

                     inputs)]]
      [:footer.modal-card-foot
       [:a.button.is-primary "Save"]
       [:a.button {:on-click close} "Cancel"]]]]))

(rum/defcs pokemon-action < rum/static
                            (rum/local false ::is-active)
  [state title img]
  (let [is-active (::is-active state)]
    [:div
     (poke-modal title ["bulba" "moltres" "pikachu"] is-active)
     [:a {:on-click #(swap! is-active not)}
      [:span.image.is-48x48 [:img {:src img}]]]]))

(rum/defcs app < (rum/local {::show-map    true
                             ::toggle-menu false})
  [state]
  (let [show-map (rum/cursor (:rum/local state) ::show-map)
        toggle-menu (rum/cursor (:rum/local state) ::toggle-menu)]
    [:div
     [:section.hero.is-light
      [:.hero-head
       [:nav.nav
        [:div.container

         [:.nav-left
          [:a.nav-item.is-brand
           [:h2.title.is-2 "Capture Me!"]]]

         [:span {:class    (str "nav-toggle" (if @toggle-menu " is-active"))
                 :on-click #(swap! toggle-menu not)}
          [:span] [:span] [:span]]

         [:div {:class (str "nav-right nav-menu" (if @toggle-menu " is-active"))}
          [:span.nav-item
           [:a.button.is-primary.is-medium
            [:span.icon.is-medium [:i.fa.fa-question-circle-o]]
            [:span "Help"]]]]]]]

      [:.hero-body
       [:nav.columns.is-mobile
        [:.column (timer)]]
       [:.card.is-fullwidth
        [:.card-image (if @show-map (pokemap))]
        [:footer.card-footer
         [:.card-footer-item.pokemon-button (pokemon-action "Pokémon" "img/pokemon.svg")]
         [:.card-footer-item.pokemon-button (pokemon-action "Pokéstop" "img/pokestop.svg")]
         [:.card-footer-item.pokemon-button (pokemon-action "Pokégym" "img/pokegym.svg")]]]]]]))

(defn init []
  (rum/mount
    (app)
    (. js/document (getElementById "app"))))
