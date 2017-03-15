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
public class Nested implements Runnable {

    Runnable child;

    public Nested(int counter, Synchronized aSynchronized)
    {
        counter--;
        if (counter > 0)
        {
            child = new Nested(counter, aSynchronized);
        }
        else
        {
            child = aSynchronized;
        }
    }

    @Override
    public void run() {
        child.run();
    }
}
