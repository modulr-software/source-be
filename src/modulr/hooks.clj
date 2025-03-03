(ns modulr.hooks)

(defn add-shutdown-hook [f]
  (let [runtime (Runtime/getRuntime)]
    (.addShutdownHook runtime (Thread. #(f)))))