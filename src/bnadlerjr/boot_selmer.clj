(ns bnadlerjr.boot-selmer
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.util :as util]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [selmer.filters :refer [add-filter!]]
            [selmer.parser :refer [render-file]]))

(defn- selmer->html
  [path]
  (.replaceAll path "\\.selmer$" ".html"))

(defn- compile-selmer!
  [in-file out-file context]
  (doto out-file
    io/make-parents
    (spit (render-file (.getName in-file)
                       context
                       :custom-resource-path (.getParent in-file)))))

(defn- load-config-file
  [path]
  (when (and path
             (.exists (io/as-file path)))
    (edn/read-string (slurp path))))

(core/deftask selmer
  "Compile Selmer templates.

  Takes all files ending in .selmer and compiles them, ignoring files that
  start with an underscore. Files with a .selmer extension that start with a
  underscore are considered to be partials loaded by the other files."
  [x context VAL str "Filename of .edn file that contains a context map that
                      will be injected into templates."
   f filters VAL str "Filename of .edn file that contains a map of filter keywords to filter function symbols."
   c cache bool "Indicate whether selmer should cache compiled templates. Defaults to true."]
  (let [tmp (core/tmp-dir!)]
    (when-not (nil? cache)
      (if cache
        (do
          (util/info "Turning Selmer cache ON\n")
          (selmer.parser/cache-on!))
        (do
          (util/info "Turning Selmer cache OFF\n")
          (selmer.parser/cache-off!))))
    (fn middleware [next-handler]
      (fn handler [fileset]
        (core/empty-dir! tmp)
        (let [in-files (core/input-files fileset)
              selmer-files (core/by-ext [".selmer"] in-files)
              context (load-config-file context)
              filters (load-config-file filters)]
          (util/info "Compiling Selmer templates...\n")
          (when filters
            (doseq [[name filter] filters]
              (when-let [fns (namespace filter)]
                (require fns))
              (add-filter! name (resolve filter))))
          (doseq [in selmer-files]
            (let [in-file (core/tmp-file in)
                  out-file (io/file tmp (selmer->html (core/tmp-path in)))]
              (when (not (s/starts-with? (.getName in-file) "_"))
                (compile-selmer! in-file out-file context)
                (util/info "• %s\n" (.getName out-file))))))
        (-> fileset
            (core/add-resource tmp)
            core/commit!
            next-handler)))))
