(ns profiled-chan.api
  (:require [clojure.core.async :as async]
            [clojure.core.async.impl.channels :as async.channels]
            [clojure.core.async.impl.protocols :as async.protocols]
            [profiled-chan.protocols :as protos]
            [profiled-chan.hdr :as hdr])
  (:import [java.util.concurrent TimeUnit]
           [java.util.concurrent.locks Lock]
           [org.HdrHistogram Histogram SingleWriterRecorder]))

;;--------------------------------------------------------------------
;; impl

(defn- wrap-handler-fn
  [h f]
  (reify
    Lock
    (lock [_] (.lock ^Lock h))
    (unlock [_] (.unlock ^Lock h))

    async.protocols/Handler
    (active? [_] (async.protocols/active? h))
    (lock-id [_] (async.protocols/lock-id h))
    (commit [_] (f (async.protocols/commit h)))))

(deftype ProfiledMMC [ch
                      ^SingleWriterRecorder put-recorder
                      ^SingleWriterRecorder take-recorder]
  protos/ProfiledChannel
  (recorders [_] {:put put-recorder
                  :take take-recorder})
  
  async.channels/MMC
  (cleanup [_] (async.channels/cleanup ch))
  (abort [_] (async.channels/abort ch))

  async.protocols/WritePort
  (put! [_ val handler]
    (if put-recorder
      (let [start (System/nanoTime)
            handler' (wrap-handler-fn
                      handler
                      (fn [f]
                        (fn [v]
                          (.recordValue put-recorder (- (System/nanoTime) start))
                          (f v))))]
        (when-let [ret (async.protocols/put! ch val handler')]
          (.recordValue put-recorder (- (System/nanoTime) start))
          ret))
      (async.protocols/put! ch val handler)))
  
  async.protocols/ReadPort
  (take! [_ handler]
    (if take-recorder
      (let [start (System/nanoTime)
            handler' (wrap-handler-fn
                      handler
                      (fn [f]
                        (fn [v]
                          (.recordValue take-recorder (- (System/nanoTime) start))
                          (f v))))]
        (when-let [ret (async.protocols/take! ch handler')]
          (.recordValue take-recorder (- (System/nanoTime) start))
          ret))
      (async.protocols/take! ch handler)))

  async.protocols/Channel
  (closed? [_] (async.protocols/closed? ch))
  (close! [_] (async.protocols/close! ch)))


;;--------------------------------------------------------------------
;; public

(defn default-recorder []
  (SingleWriterRecorder.
   (.. TimeUnit MINUTES (toNanos 1)) 2))

(defn profiled-chan
  ([ch]
   (profiled-chan ch
                  (default-recorder)
                  (default-recorder)))
  ([ch
    ^SingleWriterRecorder put-recorder
    ^SingleWriterRecorder take-recorder]
   {:pre [(satisfies? async.channels/MMC ch)]}
   (ProfiledMMC. ch put-recorder take-recorder)))

(defn profiled-chan?
  [ch]
  (satisfies? protos/ProfiledChannel ch))

(defn recorders
  [profiled-chan]
  (protos/recorders profiled-chan))


(comment

  (require '[clojure.core.async :refer [chan >!! <!! thread]]
           '[profiled-chan.api :refer [profiled-chan recorders]])

  
  (let [chan (profiled-chan (chan))
        reader (thread
                 (loop []
                   (when-some [v (<!! chan)]
                     (recur))))
        writer (thread
                 (dotimes [n 1000]
                   (>!! chan {:n n})))]
    ;; wait for writer to finish
    (<!! writer )

    ;; grab put/take recorders and output to System/out
    (let [{:keys [put take]} (recorders chan)]
      (.. put getIntervalHistogram (outputPercentileDistribution System/out 1000.0))
      (.. take getIntervalHistogram (outputPercentileDistribution System/out 1000.0))))


  )
