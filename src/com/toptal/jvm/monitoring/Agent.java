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
public class Agent extends TimerTask{
    // agentArgs usaully is k=v,k=v
    public static void premain(String agentArgs) throws InterruptedException
    {
        System.out.println("Agent initiated with: "+agentArgs);
        //parse agentArgs to arguments for Agent
        Agent agent = new Agent();
        agent.start();
    }

    Timer                     timer          = new Timer("Thread Monitoring Agent", true);
    WeakHashMap<Thread, Date> blockedThreads = new WeakHashMap<>();

    public void start()
    {
        run();
        timer.schedule(this, 500, 500);
    }

    @Override
    public void run()
    {
        long startTime = System.currentTimeMillis();
        checkThreads();
        long endTime = System.currentTimeMillis();
        long timeDiff = (endTime - startTime);
        System.out.println("It took "+timeDiff+"ms");
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
            (now - date.getTime() > 1000)
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

    private String newThreadDumpFileName(String root)
    {
        try {
            Files.createDirectories(Paths.get(root));
        } catch (IOException ex) {
            System.err.println(ex);
        }
        long   millis   = System.currentTimeMillis();
        String fileName = root + "/threads_dump_" + millis + ".txt";
        return fileName;
    }

    private void saveThreadsDump(Map<Thread, StackTraceElement[]> threads){
        String fileName = newThreadDumpFileName("tmp");
        try (PrintStream stream = new PrintStream(fileName)) {
            printThreadsDump(stream, threads);
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
        }
    }
}
