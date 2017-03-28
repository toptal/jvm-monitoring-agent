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
public class Starter implements Runnable {

    Runnable nested;

    public Starter(Synchronized aSynchronized)
    {
        nested = new Nested(100, aSynchronized);
    }

    @Override
    public void run() {
        nested.run();
    }

}
