# JVM Monitoring Agent

Monitor JVM threads and save a threads dump when threads get blocked for given 
time.

## Usage

```bash
java -javaagent:dist/jvm-monitoring-agent.jar=threshold=1000,debug ...rest of command
```

## Configuration flags/options

### `debug`

Enables debugging output - in cases you suspect something is wrong it might provide additional information.
To debug options problems make it first passed parameter.

### `root_path=...`

Specifies where to save threads dumps.

### `interval=...`

Specifies how often to check thread list for blocked threads. (Milliseconds)

### `threshold=...`

Specifies how long thread needs to be blocked before dumps will be saved. (Milliseconds)

### `delay=...`

Specifies delay between saving consecutive threads dumps. (Milliseconds)


# Examples

Examples in `examples/` are meant to check this library manually.

### Check the agent does not interrupt exit.

```bash
rm tmp/*
ant && (cd examples/empty/ && ant) &&
java -javaagent:dist/jvm-monitoring-agent.jar -jar examples/empty/dist/empty.jar
ls -l tmp/
```

There should be no output from the execution.

### Check the agent does find blocked threads.

```bash
rm tmp/*
ant && (cd examples/SynchronizedThreads/ && ant) &&
java -javaagent:dist/jvm-monitoring-agent.jar=debug,threshold=1000,delay=5000 -jar examples/SynchronizedThreads/dist/SynchronizedThreads.jar
ls -l tmp/
```

Execution is supposed to show arguments and times it took each second.
