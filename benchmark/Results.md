Benchmark Results
=================

## Throughput
This is the number of times the benchmark can be run in a second. Therefore _higher is faster_.

| Benchmark                             | Mode  | Cnt | Score         | Error         | Units |
|---------------------------------------|-------|-----|--------------:|---------------|-------|
| EventBenchmark.testASMSpeed           | thrpt |  20 |  37335742.097 | ± 796662.074 | ops/s |
| EventBenchmark.testMethodHandleSpeed  | thrpt |  20 |  27718913.782 | ± 255360.919 | ops/s |
| EventBenchmark.testReflectionSpeed    | thrpt |  20 |  30041530.671 | ± 213043.196 | ops/s |

## Average Time (in nanoseconds)
This is the average time each benchmark takes. 

| Benchmark                             | Mode | Cnt | Score  | Error   | Units |
|---------------------------------------|------|-----|-------:|---------|-------|
| EventBenchmark.testASMSpeed           | avgt | 20  | 26.750 | ± 0.284 | ns/op |
| EventBenchmark.testMethodHandleSpeed  | avgt | 20  | 37.075 | ± 1.752 | ns/op |
| EventBenchmark.testReflectionSpeed    | avgt | 20  | 33.748 | ± 0.761 | ns/op |
