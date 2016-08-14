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

(deftask deepstream-client-js []
         (comp
           (from-webjars :name "deepstream.io-client-js/dist/deepstream.min.js"
                         :target "js/deepstream.min.js")
           (sift :to-resource #{#"deepstream.min.js"})))

(deftask fontawesome []
         (comp
           (from-webjars :name "font-awesome/fonts/fontawesome-webfont.woff2"
                         :target "fonts/fontawesome-webfont.woff2")
           (from-webjars :name "font-awesome/fonts/fontawesome-webfont.woff"
                         :target "fonts/fontawesome-webfont.woff")
           (sift :to-resource #{#"woff.?$"})))

(deftask build []
         (comp (from-cljsjs)
               (sift :to-resource #{#"leaflet(.+?)inc.css"})
               (sift :move {#"^(leaflet.+?)inc.css" "css/$1css"})
               (deepstream-client-js)
               (fontawesome)
               (speak)
               (cljs)
               (sass)))

(deftask run []
         (comp (serve)
               (watch)
               (cljs-repl)
               (reload)
               (build)))

(deftask production []
         (task-options! cljs {:optimizations :simple}
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

(deftask prod
         []
         "Simple alias to mimic production mode."
         (comp (production)
               (build)
               (sift :include #{#"\.out"} :invert true)))
