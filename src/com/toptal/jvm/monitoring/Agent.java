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
    public static void premain(String stringArgs) throws InterruptedException
    {
        Agent agent = new Agent(stringArgs);
        agent.start();
    }

    // configuration & defaults
    boolean debug     = false; //turn on debugging
    String  root_path = "tmp"; //where to save dumps
    int     interval  = 1000;  //interval between checks in milliseconds
    int     threshold = 60000; //how long thread needs to be blocked to save the dump in milliseconds

    // internal
    Timer                     timer          = new Timer("Thread Monitoring Agent", true);
    WeakHashMap<Thread, Date> blockedThreads = new WeakHashMap<>();

    // stringArgs usaully is k=v,k=v
    public Agent(String stringArgs)
    {
        if (stringArgs == null) stringArgs = "";
        String[] args = stringArgs.split(",");
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

    public void log(String msg)
    {
        if (debug)
            System.err.println("[JVM Monitoring Agent] " + msg);
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

    private void checkThreads()
    {
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();

        cleanUnBlockedThreads();
        addBlockedThreads(threads.keySet());

        //printThreadsDump(System.out, threads);
        if (blockedToLong())
        {
            saveThreadsDump(threads);
        }
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
            String deamon = "";
            if (thread.isDaemon()) deamon = "deamon ";
            stream.format(
                "Thread:%d '%s' %sprio=%d %s%n",
                thread.getId(),
                thread.getName(),
                deamon,
                thread.getPriority(),
                thread.getState()
            );
            for (StackTraceElement line: stack)
            {
                stream.println("        " + line);
            }
            stream.println();
        });
    }

    private String newThreadDumpFileName()
    {
        try {
            Files.createDirectories(Paths.get(root_path));
        } catch (IOException ex) {
            log(ex.toString());
        }
        long timeStamp = System.currentTimeMillis();
        return root_path + "/threads_dump_" + timeStamp + ".txt";
    }

    private void saveThreadsDump(Map<Thread, StackTraceElement[]> threads){
        String fileName = newThreadDumpFileName();
        try (PrintStream stream = new PrintStream(fileName)) {
            printThreadsDump(stream, threads);
        } catch (FileNotFoundException ex) {
            log(ex.toString());
        }
    }
}
