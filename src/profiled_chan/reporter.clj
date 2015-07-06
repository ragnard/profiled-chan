(ns profiled-chan.reporter
  (:require [clojure.core.async :as async]
            [profiled-chan.api :refer [recorders profiled-chan profiled-chan?]]
            [profiled-chan.hdr :as hdr])
  (:import [org.HdrHistogram Histogram SingleWriterRecorder]))

(defn- update-histogram
  [chan h]
  (let [{:keys [put take]} (recorders chan)]
    {:put (if (:put h) 
            (.getIntervalHistogram put (:put h))
            (.getIntervalHistogram put))
     :take (if (:take h) 
             (.getIntervalHistogram take (:take h))
             (.getIntervalHistogram take))}))

(defn- update-histograms
  [histograms chans]
  (reduce (fn [res chan]
            (assoc res chan (update-histogram chan (histograms chan))))
          histograms
          chans))

(defn reporter
  ([profiled-chans out]
   (reporter profiled-chans out {}))
  ([profiled-chans out {:keys [interval-ms stats-fn]
                         :or {interval-ms 5000
                              stats-fn hdr/stats}}]
   {:pre [(every? profiled-chan? profiled-chans)]}
   (let [ctl (async/chan)]
     (async/thread
       (loop [histograms {}]
         (async/alt!!
           (async/timeout interval-ms)
           ([_]
            (let [updated (update-histograms histograms profiled-chans)]
              (run! (fn [[chan {:keys [put take]}]]
                      (async/>!! out
                                 {:chan chan
                                  :put (stats-fn put)
                                  :take (stats-fn take)}))
                    updated)
              (recur updated)))

           ctl
           ([_]))))
     ctl)))






(comment

  (def c (profiled-chan (async/chan)))

  (do
    (async/thread
      (loop [n 0]
        (when (async/>!! c {:n n})
          (recur (inc n))))
      (println "done putting")
      (async/close! c))
    (dotimes [n 4]
      (async/thread
        (loop [v (async/<!! c)]
          (when v
            (recur (async/<!! c))))
        (println "done taking"))))

  (async/close! c)

  
  
  (def stats-out (async/chan))
  
  (def ctl (reporter [c] stats-out))

  (async/<!! stats-out)

  (async/close! ctl)
  
  )
