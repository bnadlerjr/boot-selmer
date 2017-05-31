(set-env!
 :src-paths    #{"src"}
 :dependencies '[[adzerk/bootlaces "0.1.11" :scope "test"]
                 [selmer "1.10.7"]])

(require '[adzerk.bootlaces :refer :all])

(def +version+ "0.0.1-SNAPSHOT")
(bootlaces! +version+)

(task-options!
 pom {:project     'bnadlerjr/boot-selmer
      :version     +version+
      :description "Selmer Boot task"
      :url         "https://github.com/bnadlerjr/boot-selmer"
      :scm         {:url "https://github.com/bnadlerjr/boot-selmer"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})
