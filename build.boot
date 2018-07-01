(set-env! :dependencies '[[seancorfield/boot-tools-deps "0.4.5" :scope "test"]])

(require '[boot-tools-deps.core :refer [deps]])

(def project 'snow)
(def version "0.3.1")

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
  (comp (deps) (pom ) (jar) (install)))

(require '[adzerk.bootlaces :refer :all])
(bootlaces! version)

(deftask publish []
  (comp
   (deps)
   (pom)
   (jar)
   (push-release)))

