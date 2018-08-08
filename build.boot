(set-env! :dependencies '[[seancorfield/boot-tools-deps "0.4.5" :scope "test"]
                          [entranceplus/bootlaces "0.1.14"]]
          :repositories #(conj %
                               ["jboss" {:url "https://repository.jboss.org/nexus/content/groups/public/"}]
                               ["jitpack" {:url "https://jitpack.io"}]))

(require '[boot-tools-deps.core :refer [deps]])

(def project 'snow)
(def version "0.4.2")

(task-options!
 pom {:project     project
      :version     version
      :description "Snow"
      :scm         {:url "https://github.com/entranceplus/snow"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build
  "Build and install the project locally."
  []
  (comp (deps :quick-merge true) (pom ) (jar) (install)))

(require '[adzerk.bootlaces :refer :all])
(bootlaces! version)

(deftask publish []
  (comp
   (deps :quick-merge true)
   (pom)
   (build-jar)
   (push-release)))

