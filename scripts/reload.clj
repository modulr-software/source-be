#!/usr/bin/env bb
;; reload.clj — babashka bencode nREPL client for the source-be reload workflow.
;;
;; Reads `.nrepl-port` from cwd, opens a TCP socket to localhost:<port>,
;; bencode-encodes an `eval` op for (do (require 'dev) (dev/reload)), drains
;; responses until `done`, prints stdout/stderr, exits non-zero on eval/syntax
;; error. Run from the project root:
;;
;;   bb scripts/reload.clj
;;
;; If `.nrepl-port` is missing it prints a clear message and exits 1 — do not
;; try to start nREPL yourself; ask the user to run ./nrepl.sh.
(ns reload
  (:require [bencode.core :as b]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net Socket]
           [java.io ByteArrayOutputStream File]))

(defn- port-file []
  (let [f (io/file ".nrepl-port")]
    (when (and (.exists f) (pos? (.length f))) f)))

(defn- read-port [^File f]
  (some-> (re-find #"\d+" (str/trim (slurp f))) parse-long))

(defn- ->str [x]
  (cond (nil? x) nil (string? x) x (bytes? x) (String. ^bytes x "UTF-8") :else (str x)))

(defn- send-msg! [out msg]
  (let [ba (ByteArrayOutputStream.)]
    (b/write-bencode ba msg)
    (.write out (.toByteArray ba)))
  (.flush out))

(defn- done? [m]
  (let [s (get m "status")]
    (and (sequential? s) (some #(= "done" (->str %)) s))))

(defn- read-resps [in]
  (let [rdr (java.io.PushbackInputStream. in)]
    (loop [resps []]
      (let [msg (try (b/read-bencode rdr)
                     (catch Exception e
                       (if (instance? java.io.EOFException e) ::eof (throw e))))]
        (cond (identical? ::eof msg) resps
              (nil? msg) resps
              (done? msg) (conj resps msg)
              :else (recur (conj resps msg)))))))

(defn- summarize [resps]
  (let [outs   (keep #(-> % (get "out") ->str) resps)
        errs   (keep #(-> % (get "err") ->str) resps)
        status (->> resps (keep #(get % "status")) (mapcat #(map ->str %)))
        ex     (some #(-> % (get "ex") ->str) resps)
        root-ex (some #(-> % (get "root-ex") ->str) resps)
        err?   (or (some #{"eval-error" "syntax-error" "unknown-op" "load-error"} status)
                   (some? ex))]
    (when (seq outs) (print (str/join outs)))
    (when (seq errs) (binding [*out* *err*] (print (str/join errs))))
    (when err?
      (when ex      (binding [*out* *err*] (println "\n[reload] eval error:" ex)))
      (when root-ex (binding [*out* *err*] (println "[reload] root exception:" root-ex))))
    (flush)
    {:ok (not err?) :status status}))

(defn -main [& _args]
  (let [pf (port-file)]
    (cond
      (nil? pf)
      (do (println "[reload] no .nrepl-port in cwd; is ./nrepl.sh running?")
          (System/exit 1))

      :else
      (let [port (read-port pf)]
        (if-not port
          (do (println "[reload] could not parse port from .nrepl-port")
              (System/exit 1))
          (with-open [sock (Socket. "localhost" (int port))]
            (let [in  (.getInputStream sock)
                  out (.getOutputStream sock)]
              (send-msg! out {"op" "eval" "code" "(do (require 'dev) (dev/reload))"})
              (let [r (summarize (read-resps in))]
                (System/exit (if (:ok r) 0 1)))))))))

(apply -main *command-line-args*)
