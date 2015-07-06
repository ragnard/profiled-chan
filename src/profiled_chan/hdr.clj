(ns profiled-chan.hdr
  (:import [org.HdrHistogram Histogram SingleWriterRecorder]))

(defn percentile-values
  [^Histogram hist percentiles]
  (reduce (fn [res p]
            (assoc res p (.getValueAtPercentile hist p)))
          {}
          percentiles))

(defn stats
  ([^Histogram hist]
   (stats hist [50 75 90 95 98 99]))
  ([^Histogram hist percentiles]
   (let [start (.getStartTimeStamp hist)
         end (.getEndTimeStamp hist)
         count (.getTotalCount hist)
         duration (- end start)]
     {:start start
      :end end
      :count count
      :duration duration
      :rate-ms (double (/ count duration))
      :min (.getMinValue hist)
      :max (.getMaxValue hist)
      :mean (.getMean hist)
      :std-dev (.getStdDeviation hist)
      :percentiles (percentile-values hist percentiles)})))


