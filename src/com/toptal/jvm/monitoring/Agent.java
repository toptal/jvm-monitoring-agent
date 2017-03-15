/*
 * license...
 */
package com.toptal.jvm.monitoring;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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

    Timer timer = new Timer("Thread Monitoring Agent", true);

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
        Set<Map.Entry<Thread, StackTraceElement[]>> threads = Thread.getAllStackTraces().entrySet();
        System.out.println(" # Threads: "+threads.size());
        threads.forEach((thread_entry) -> {
            Thread thread = thread_entry.getKey();
            StackTraceElement[] stackTrace = thread_entry.getValue();
            System.out.format(
                "Thread = %s, Status = %s%n%s%n",
                thread,
                thread.getState(),
                Arrays.toString(stackTrace)
            );
        });
    }
}
