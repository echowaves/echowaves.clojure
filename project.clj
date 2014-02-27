(defproject echowaves "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.6"]
                 [ring-server "0.3.1"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [korma "0.3.0-RC6"]
                 [log4j "1.2.15" 
                  :exclusions [javax.mail/mail
                               javax.jms/jms
                               com.sun.jdmk/jmxtools
                               com.sun.jmx/jmxri]]
                 [com.taoensso/timbre "2.6.1"]
                 [com.postspectacular/rotor "0.1.0"]
                 [lib-noir "0.6.9"]
                 [selmer "0.4.2"]
                 [org.clojure/tools.reader "0.8.2"]
                 [org.clojure/clojurescript "0.0-2120"]
                 [domina "1.0.2"]
                 [cljs-ajax "0.2.3"]
                 [ring-middleware-format "0.3.1"]
                 [environ "0.4.0"]
                 [commons-fileupload "1.3"]
                 [ragtime "0.3.4"]
                 [org.clojars.gzeureka/javapns "2.2"]
                 [bouncycastle/bcprov-jdk16-nosign "140"]]
  :plugins [[lein-ring "0.8.8"]
            [lein-cljsbuild "1.0.1"]
            [lein-environ "0.4.0"]
            [ragtime/ragtime.lein "0.3.4"]]
  :ring {:handler echowaves.handler/war-handler
         :init echowaves.handler/init
         :destroy echowaves.handler/destroy}
  :ragtime {:migrations ragtime.sql.files/migrations
            :database (str "jdbc:mysql:" (System/getenv "EW_DB_URL") "?user=" (System/getenv "EW_DB_USER") "&password=" (System/getenv "EW_DB_PASS"))}
  
  :profiles
  {
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}
    :env {:ew-db-url "//localhost:3306/echowaves"
          :ew-db-user "echowaves"
          :ew-db-pass "secret"
          :ew-push-cert "EWPush-dev.p12"
          :ew-push-cert-pass "password"
          :ew-push-prod false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.2.0"]]
    :env {:ew-db-url "//localhost:3306/echowaves"
          :ew-db-user "echowaves"
          :ew-db-pass "echowaves"
          :ew-push-cert "EWPush-dev.p12"
          :ew-push-cert-pass "password"
          :ew-push-prod false}}}  
  :cljsbuild
  {:builds
   {:dev {:source-paths ["src-cljs"]
          :compiler
          {:pretty-print true
           :output-to "resources/public/js/echowaves-cljs.js"}}
    :prod {:source-paths ["src-cljs"]
           :compiler
           {:optimizations :advanced
            :externs ["resources/externs.js"]
            :output-to "resources/public/js/echowaves-cljs.js"}}}})
