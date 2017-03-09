/*
 * license...
 */
package com.toptal.jvm.monitoring;

/**
 *
 * @author mpapis
 */
public class Agent {
    public static void premain(String agentArgs)
    {
        System.out.println("Agent initiated with: "+agentArgs);
        return;
    }
}
