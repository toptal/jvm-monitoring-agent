/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.toptal;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author mpapis
 */
public class SynchronizedThreads {

    /**
     * @param args the command line arguments
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        List<Thread> threads = new ArrayList<Thread>();
        Synchronized aSynchronized = new Synchronized();
        for (int i=0; i<1000; i++)
        {
            Thread thread = new Thread(new Starter(aSynchronized));
            threads.add(thread);
            thread.start();
        }
        for (Thread thread: threads)
        {
            thread.join();
        }
    }

}
