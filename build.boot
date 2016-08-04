(set-env!
  :source-paths #{"sass" "src/cljs"}
  :resource-paths #{"resources"}
  :dependencies '[[adzerk/boot-cljs "1.7.228-1" :scope "test"]
                  [adzerk/boot-cljs-repl "0.3.2" :scope "test"]
                  [cljsjs/boot-cljsjs "0.5.1" :scope "test"]
                  [adzerk/boot-reload "0.4.8" :scope "test"]
                  [pandeiro/boot-http "0.7.2" :scope "test"]
                  [com.cemerick/piggieback "0.2.1" :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12" :scope "test"]
                  [weasel "0.7.0" :scope "test"]
                  [deraen/boot-sass "0.2.1" :scope "test"]
                  [org.slf4j/slf4j-nop "1.7.13" :scope "test"]

                  [org.clojure/clojurescript "1.7.228"]
                  [cljsjs/leaflet-locatecontrol "0.43.0-1"]
                  [cljsjs/react-leaflet "0.11.4-1"]
                  [org.webjars/font-awesome "4.6.3"]
                  [org.webjars.bower/deepstream.io-client-js "1.0.2"]
                  [rum "0.10.4"]])

(require
  '[adzerk.boot-cljs :refer [cljs]]
  '[cljsjs.boot-cljsjs :refer [from-cljsjs from-webjars]]
  '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
  '[adzerk.boot-reload :refer [reload]]
  '[pandeiro.boot-http :refer [serve]]
  '[deraen.boot-sass :refer [sass]])

(deftask build []
         (comp (speak)
               (cljs)
               (sass)))

(deftask deepstream-client-js []
         (comp
           (from-webjars :name "deepstream.io-client-js/dist/deepstream.js"
                         :target "js/deepstream.js")))

(deftask fontawesome []
         (comp
           (from-webjars :name "font-awesome/fonts/fontawesome-webfont.woff2"
                         :target "fonts/fontawesome-webfont.woff2")
           (from-webjars :name "font-awesome/fonts/fontawesome-webfont.woff"
                         :target "fonts/fontawesome-webfont.woff")
           (from-webjars :name "font-awesome/css/font-awesome.css"
                         :target "css/font-awesome.css")))

(deftask run []
         (comp (serve)
               (watch)
               (from-cljsjs)
               (deepstream-client-js)
               (fontawesome)
               (cljs-repl)
               (reload)
               (build)))

(deftask production []
         (task-options! cljs {:optimizations :advanced}
                        sass {:output-style :compressed})
         identity)

(deftask development []
         (task-options! cljs {:optimizations :none :source-map true}
                        reload {:on-jsload 'capture-me.app/init})
         identity)

(deftask dev
         "Simple alias to run application in development mode"
         []
         (comp (development)
               (run)))
