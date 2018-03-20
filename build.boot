(def project 'snow)
(def version "0.1.0-SNAPSHOT")

(set-env! :resource-paths #{"resources" "src"}
          :source-paths   #{"test"}
          :dependencies   '[[org.clojure/clojure "RELEASE"]
                            [ragtime "0.7.2"]
                            [org.clojure/java.jdbc "0.7.1"]
                            [clj-http "3.7.0"]
                            [org.postgresql/postgresql "42.1.4"]
                            [conman "0.6.8"]
                            [nilenso/honeysql-postgres "0.2.3"]
                            [honeysql "0.9.1"]
                            [tailrecursion/boot.core "2.5.1"]
                            [com.brunobonacci/safely "0.5.0-alpha3"]
                            [environ "1.1.0"]
                            [metosin/muuntaja "0.5.0"]
                            [org.danielsz/system "0.4.2-SNAPSHOT"]
                            [com.cemerick/friend "0.2.3"]
                            [adzerk/boot-test "RELEASE" :scope "test"]])

(task-options!
 pom {:project     project
      :version     version
      :description "FIXME: write description"
      :url         "http://example/FIXME"
      :scm         {:url "https://github.com/yourname/snow"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (pom) (jar) (install)))

(require '[adzerk.boot-test :refer [test]])
