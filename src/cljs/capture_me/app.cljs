(ns capture-me.app
  (:require [cljsjs.leaflet-locatecontrol]
            [cljsjs.react-leaflet]
            [camel-snake-kebab.core :refer [->kebab-case-string]]
            [rum.core :as rum]
            [sablono.core :refer-macros [html]]))

(enable-console-print!)

(def app-state (atom {:position     [35.0853 -106.6056]
                      :current-time (.getTime (js/Date.))}))

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
    (js/React.createElement js/ReactLeaflet.Map (clj->js {:zoom 13 :center position :id "pokemap"})
                            (js/React.createElement js/ReactLeaflet.TileLayer (clj->js tilelayer-options))
                            (js/React.createElement js/ReactLeaflet.Marker (clj->js {:position position
                                                                                     :icon     (pokemon-icon 1)})
                                                    (js/React.createElement js/ReactLeaflet.Popup #js {} (html [:span "Albuquerque"]))))))

(rum/defc timer < rum/reactive
                  {:will-mount   (fn [state]
                                   (let [current-time (rum/cursor app-state :current-time)]
                                     (assoc state ::interval-id (js/setInterval #(reset! current-time (.getTime (js/Date.))) 1000))))
                   :will-unmount (fn [state]
                                   (do
                                     (js/clearInterval (::interval-id state))
                                     (assoc state ::interval-id nil)))}
  []
  [:p.heading (str "Time: " (:current-time (rum/react app-state)))])

(rum/defc pokemon-button
  [name class on-click]
  [:a {:class    (str "button card-footer-item is-large " class)
       :on-click on-click}
   name])

(rum/defc pokemon-modal
  [title is-active]
  (let [inputs ["Pokemon Name" "Location" "Time"]
        kebab #(->kebab-case-string %)
        class (if @is-active {:class "is-active"})
        close #(swap! is-active not)]
    [:.modal class
     [:.modal-background {:on-click close}]
     [:.modal-card
      [:.modal-card-head
       [:p.modal-card-title title]
       [:button.delete {:on-click close}]]
      [:section.modal-card-body
       [:.content
        (map-indexed #(vector :p.control {:key %1}
                              [:label.label {:for (kebab %2)} %2]
                              [:input.input {:id (kebab %2) :type "text"}])
                     inputs)]]
      [:footer.modal-card-foot
       [:a.button.is-primary "Save"]
       [:a.button {:on-click close} "Cancel"]]]]))

(rum/defcs app < (rum/local {::show-map     true
                             ::show-sighted false})
  [state]
  (let [show-map (rum/cursor (:rum/local state) ::show-map)
        show-sighted (rum/cursor (:rum/local state) ::show-sighted)]

    [:div
     [:section.hero.is-light
      [:.hero-head
       [:nav.nav
        [:div.container

         [:.nav-left
          [:a.nav-item.is-brand
           [:h2.title.is-2 "Capture Me!"]]]

         [:span.nav-toggle [:span] [:span] [:span]]

         [:.nav-right.nav-menu
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
         (pokemon-button "Sighted!" "is-warning" #(swap! show-sighted not))
         (pokemon-button "Caught!" "is-success" #(swap! show-sighted not))]]]]
     (pokemon-modal "Pokemon Sighted!" show-sighted)]))

(defn init []
  (rum/mount
    (app)
    (. js/document (getElementById "app"))))
