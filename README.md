# profiled-chan

**WIP**

```clojure
(require '[clojure.core.async :refer [chan >!! <!! thread]]
         '[profiled-chan.api :refer [profiled-chan recorders]])

(let [chan (profiled-chan (chan))
      writer (thread
               (dotimes [n 1000]
                 (>!! chan n)))
      reader (thread
               (loop []
                 (when-some [v (<!! chan)]
                   (recur))))]
  ;; wait for writer to finish
  (<!! writer)
  
  ;; grab put/take recorders and output to System/out
  (let [{:keys [put take]} (recorders chan)]
    (.. put getIntervalHistogram (outputPercentileDistribution System/out 1000.0))
    (.. take getIntervalHistogram (outputPercentileDistribution System/out 1000.0))))
```

Output:

```shell
Value     Percentile TotalCount 1/(1-Percentile)

1.31 0.000000000000          1           1.00
1.94 0.100000000000        101           1.11
2.16 0.200000000000        205           1.25
2.35 0.300000000000        302           1.43
2.70 0.400000000000        400           1.67
8.96 0.500000000000        500           2.00
10.37 0.550000000000        550           2.22
11.26 0.600000000000        601           2.50
11.90 0.650000000000        651           2.86
12.61 0.700000000000        700           3.33
13.31 0.750000000000        752           4.00
13.82 0.775000000000        779           4.44
14.27 0.800000000000        802           5.00
14.85 0.825000000000        827           5.71
15.36 0.850000000000        850           6.67
16.32 0.875000000000        877           8.00
16.51 0.887500000000        888           8.89
16.90 0.900000000000        902          10.00
17.41 0.912500000000        914          11.43
18.30 0.925000000000        925          13.33
19.20 0.937500000000        938          16.00
19.84 0.943750000000        947          17.78
20.10 0.950000000000        950          20.00
20.86 0.956250000000        957          22.86
21.89 0.962500000000        964          26.67
22.91 0.968750000000        969          32.00
24.70 0.971875000000        972          35.56
30.46 0.975000000000        975          40.00
55.30 0.978125000000        979          45.71
76.80 0.981250000000        983          53.33
101.38 0.984375000000        985          64.00
118.27 0.985937500000        986          71.11
133.12 0.987500000000        988          80.00
134.14 0.989062500000        990          91.43
135.17 0.990625000000        991         106.67
153.60 0.992187500000        993         128.00
153.60 0.992968750000        993         142.22
164.86 0.993750000000        994         160.00
166.91 0.994531250000        995         182.86
172.03 0.995312500000        996         213.33
175.10 0.996093750000        997         256.00
175.10 0.996484375000        997         284.44
175.10 0.996875000000        997         320.00
177.15 0.997265625000        998         365.71
177.15 0.997656250000        998         426.67
179.20 0.998046875000        999         512.00
179.20 0.998242187500        999         568.89
179.20 0.998437500000        999         640.00
179.20 0.998632812500        999         731.43
179.20 0.998828125000        999         853.33
185.34 0.999023437500       1000        1024.00
185.34 1.000000000000       1000
#[Mean    =        10.93, StdDeviation   =        19.69]
#[Max     =       185.34, Total count    =         1000]
#[Buckets =           29, SubBuckets     =          256]
Value     Percentile TotalCount 1/(1-Percentile)

1.40 0.000000000000          1           1.00
1.98 0.100000000000        100           1.11
2.19 0.200000000000        209           1.25
2.38 0.300000000000        306           1.43
2.58 0.400000000000        403           1.67
9.34 0.500000000000        500           2.00
11.46 0.550000000000        556           2.22
12.48 0.600000000000        600           2.50
13.38 0.650000000000        651           2.86
14.53 0.700000000000        702           3.33
15.62 0.750000000000        756           4.00
16.13 0.775000000000        776           4.44
17.02 0.800000000000        801           5.00
17.79 0.825000000000        826           5.71
18.82 0.850000000000        851           6.67
19.97 0.875000000000        875           8.00
20.74 0.887500000000        893           8.89
21.12 0.900000000000        900          10.00
21.89 0.912500000000        913          11.43
22.66 0.925000000000        927          13.33
24.70 0.937500000000        940          16.00
25.73 0.943750000000        944          17.78
26.50 0.950000000000        950          20.00
28.42 0.956250000000        957          22.86
29.82 0.962500000000        964          26.67
33.54 0.968750000000        969          32.00
36.86 0.971875000000        972          35.56
39.68 0.975000000000        975          40.00
52.48 0.978125000000        979          45.71
72.70 0.981250000000        982          53.33
122.88 0.984375000000        985          64.00
125.95 0.985937500000        986          71.11
132.10 0.987500000000        988          80.00
136.19 0.989062500000        990          91.43
138.24 0.990625000000        991         106.67
152.58 0.992187500000        993         128.00
152.58 0.992968750000        993         142.22
161.79 0.993750000000        994         160.00
166.91 0.994531250000        995         182.86
172.03 0.995312500000        996         213.33
176.13 0.996093750000        997         256.00
176.13 0.996484375000        997         284.44
176.13 0.996875000000        997         320.00
182.27 0.997265625000        998         365.71
182.27 0.997656250000        998         426.67
191.49 0.998046875000        999         512.00
191.49 0.998242187500        999         568.89
191.49 0.998437500000        999         640.00
191.49 0.998632812500        999         731.43
191.49 0.998828125000        999         853.33
356.35 0.999023437500       1000        1024.00
356.35 1.000000000000       1000
#[Mean    =        12.35, StdDeviation   =        22.71]
#[Max     =       356.35, Total count    =         1000]
#[Buckets =           29, SubBuckets     =          256]
Value     Percentile TotalCount 1/(1-Percentile)
```

## License

Copyright © 2015 Ragnar Dahlen.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.