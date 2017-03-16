/*
 * license...
 */
package com.toptal.jvm.monitoring;

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
        System.out.println(" # Threads: "+threads.size()+" Blocked: "+blockedThreads.size()+" flag: "+blockedToLong());
    }

    private void cleanUnBlockedThreads()
    {
        blockedThreads.keySet().removeIf(thread -> (
            thread.getState() != Thread.State.BLOCKED
        ));
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
}
