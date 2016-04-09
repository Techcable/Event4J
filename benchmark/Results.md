Benchmark Results
=================

## Throughput
This is the number of times the benchmark can be run in a second. Therefore _higher is faster_.

| Benchmark                             | Mode  | Cnt | Score         | Error         | Units |
|---------------------------------------|-------|-----|--------------:|---------------|-------|
| EventBenchmark.testASMSpeed           | thrpt |  20 | 284445185.095 | ± 8136139.219 | ops/s |
| EventBenchmark.testMethodHandleSpeed  | thrpt |  20 |  28862054.868 | ±  722179.825 | ops/s |
| EventBenchmark.testReflectionSpeed    | thrpt |  20 |  32500300.846 | ±  182061.172 | ops/s |

## Average Time (in nanoseconds)
This is the average time each benchmark takes. 

| Benchmark                             | Mode | Cnt | Score  | Error   | Units |
|---------------------------------------|------|-----|-------:|---------|-------|
| EventBenchmark.testASMSpeed           | avgt | 20  |  3.238 | ± 0.148 | ns/op |
| EventBenchmark.testMethodHandleSpeed  | avgt | 20  | 33.661 | ± 0.107 | ns/op |
| EventBenchmark.testReflectionSpeed    | avgt | 20  | 31.177 | ± 0.124 | ns/op |
