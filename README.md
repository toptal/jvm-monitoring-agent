# JVM Monitoring Agent

Monitor JVM threads and save a threads dump when threads get blocked for given 
time.

# Examples

Examples in `examples/` are meant to check this library manually.

### Check the agent does not interrupt exit.

```bash
ant && (cd examples/empty/ && ant) &&
java -javaagent:dist/jvm-monitoring-agent.jar -jar examples/empty/dist/empty.jar
```

### Check the agent does find blocked threads.

```bash
ant && (cd examples/SynchronizedThreads/ && ant) &&
java -javaagent:dist/jvm-monitoring-agent.jar -jar examples/SynchronizedThreads/dist/SynchronizedThreads.jar
```
