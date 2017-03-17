/*
 * license...
 */
package com.toptal.jvm.monitoring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

/**
 *
 * @author mpapis
 */
public final class Agent extends TimerTask{

    // configuration & defaults
    private boolean debug     = false; //turn on debugging
    private String  root_path = "tmp"; //where to save dumps
    private int     interval  = 1000;  //interval between checks in milliseconds
    private int     threshold = 60000; //how long thread needs to be blocked to save the dump in milliseconds

    // internal
    private static final String             NAME           = "JVM Monitoring Agent";
    private final Timer                     timer          = new Timer(NAME, true);
    private final WeakHashMap<Thread, Date> blockedThreads = new WeakHashMap<>();

    public static void premain(String stringArgs) throws InterruptedException
    {
        Agent agent = new Agent(stringArgs);
        agent.start();
    }

    public Agent(String stringArgs)
    {
        parseArgs(stringArgs);
        createRootPath();
    }

    private void parseArgs(String stringArgs) {
        String[] args = stringArgs == null ? new String[0] : stringArgs.split(",");
        for (String arg: args)
        {
            String[] key_value = arg.split("=", 2);
            switch (key_value[0]) {
                case "":          break; // in case of no args just skip
                case "debug":     debug      = true;                           break;
                case "root":      root_path  = key_value[1];                   break;
                case "interval":  interval   = Integer.parseInt(key_value[1]); break;
                case "threshold": threshold  = Integer.parseInt(key_value[1]); break;
                default:
                    log("Unknown argument:" + arg);
                    break;
            }
        }
        log(String.format(
            "Initiated with:%n  root: '%s'%n  interval: %d%n  treshold: %d%n",
            root_path,
            interval,
            threshold
        ));
    }

    private void createRootPath() {
        try {
            Files.createDirectories(Paths.get(root_path));
        } catch (IOException ex) {
            log(ex.toString());
        }
    }

    public void start()
    {
        run();
        timer.schedule(this, interval, interval);
    }

    @Override
    public void run()
    {
        long startTime = System.currentTimeMillis();
        checkThreads();
        long endTime = System.currentTimeMillis();
        long timeDiff = (endTime - startTime);
        log("It took "+timeDiff+"ms");
    }

    private void log(String msg)
    {
        if (debug)
            System.err.println("[" + NAME + "] " + msg);
    }

    private void checkThreads()
    {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();

        cleanUnBlockedThreads();
        addBlockedThreads(threads.keySet());

        if (blockedToLong())
            saveThreadsDump(threads);
    }

    private void cleanUnBlockedThreads()
    {
        blockedThreads.keySet().removeIf(thread ->
            (thread.getState() != Thread.State.BLOCKED)
        );
    }

    private void addBlockedThreads(Set<Thread> threads)
    {
        threads.stream().filter(thread ->
            (thread.getState() == Thread.State.BLOCKED)
        ).forEach(thread ->
            blockedThreads.putIfAbsent(thread, new Date())
        );
    }

    private boolean blockedToLong()
    {
        long now = new Date().getTime();
        return blockedThreads.values().stream().anyMatch(date ->
            (now - date.getTime() > threshold)
        );
    }

    private void printThreadsDump(PrintStream stream, Map<Thread, StackTraceElement[]> threads)
    {
        stream.format(
            "#Threads: %d, #Blocked: %d%n%n",
            threads.size(),
            blockedThreads.size()
        );
        threads.forEach((thread, stack) -> {
            stream.format(
                "Thread:%d '%s' %sprio=%d %s%n",
                thread.getId(),
                thread.getName(),
                thread.isDaemon() ? "deamon " : "",
                thread.getPriority(),
                thread.getState()
            );
            for (StackTraceElement line: stack)
                stream.println("        " + line);
            stream.println();
        });
    }

    private void saveThreadsDump(Map<Thread, StackTraceElement[]> threads){
        long timeStamp  = System.currentTimeMillis();
        String fileName = root_path + "/threads_dump_" + timeStamp + ".txt";

        try (PrintStream stream = new PrintStream(fileName)) {
            printThreadsDump(stream, threads);
        } catch (FileNotFoundException ex) {
            log(ex.toString());
        }
    }
}
