(ns bnadlerjr.boot-selmer
  {:boot/export-tasks true}
  (:require [boot.core :as core]
            [boot.util :as util]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as s]
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

(defn- load-context-file
  [path]
  (when (and path
             (.exists (io/as-file path)))
    (edn/read-string (slurp path))))

(core/deftask selmer
  "Compile Selmer templates.

  Takes all files ending in .selmer and compiles them, ignoring files that
  start with an underscore. Files with a .selmer extension that start with a
  underscore are considered to be partials loaded by the other files."
  [_ config VAL str "Filename of .edn file that contains a context map that
                    will be injected into templates"]
  (let [tmp (core/tmp-dir!)]
    (fn middleware [next-handler]
      (fn handler [fileset]
        (core/empty-dir! tmp)
        (let [in-files (core/input-files fileset)
              selmer-files (core/by-ext [".selmer"] in-files)
              context (load-context-file config)]
          (util/info "Compiling Selmer templates...\n")
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
