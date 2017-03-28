/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.toptal;

/**
 *
 * @author mpapis
 */
public class Synchronized implements Runnable {
    @Override
    public synchronized void run()
    {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            //
        }
    }
}
