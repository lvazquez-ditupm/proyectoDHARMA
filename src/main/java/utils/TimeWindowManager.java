/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import core.BayesNetworkManager;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class TimeWindowManager implements Runnable {

    private final int seconds;
    private final double probIncrement;
    private final String node;
    private final BayesNetworkManager bayesNet;

    public TimeWindowManager(int seconds, double probIncrement, String node, BayesNetworkManager bayesNet) {
        this.seconds = seconds;
        this.probIncrement = probIncrement;
        this.node = node;
        this.bayesNet = bayesNet;
    }
    
    /**
     * Esta función calcula el periodo durante el cual se aumenta la
     * probabilidad de ocurrencia de los eventos en base al evento ocurrido
     *
     * @return segundos de ventana
     */
    private int getTimeWindow() {
        //TODO
        return 10;
    }

    /**
     * Esta función calcula cuánto debe aumentarse la probabilidad de ocurrencia
     * de los eventos en base al evento ocurrido
     *
     * @return aumento de probabilidad
     */
    private double getProbIncrement() {
        //TODO
        return 0.3;
    }

    @Override
    public void run() {
        try {
            bayesNet.timeWindow(node, probIncrement);
            Thread.sleep(seconds * 1000);
            bayesNet.timeWindow(node, -probIncrement);
        } catch (Exception ex) {
            Logger.getLogger(TimeWindowManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
