/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import java.util.HashMap;

/**
 * This class recolects super-events and data maps from sensors, in order to
 * give them to the reasoner
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class ReasonerInput {

    private static HashMap<Long, HashMap> superEvents = new HashMap<>();
    private static HashMap<Long, HashMap> dataMaps = new HashMap<>();

    public static void newSuperEvent(HashMap superEvent) {
        //TODO Llevar a una base de datos en lugar de variables
        long timestamp = (long) superEvent.get("Timestamp");
        if (dataMaps.containsKey(timestamp)) {
            prepareReasoner(dataMaps.get(timestamp), superEvents.get(timestamp));
        } else {
            superEvents.put(timestamp, superEvent);
        }
    }

    public static void newDataMap(HashMap dataMap) {
        //TODO Llevar a una base de datos en lugar de variables
        long timestamp = (long) dataMap.get("Timestamp");
        if (superEvents.containsKey(timestamp)) {
            prepareReasoner(dataMaps.get(timestamp), superEvents.get(timestamp));
        } else {
            dataMaps.put(timestamp, dataMap);
        }
    }

    private static void prepareReasoner(HashMap dataMap, HashMap superEvent) {
        //TODO
        System.out.println("DATAMAP: " + dataMap.toString());
        System.out.println("SUPEREVENTO: " + superEvent.toString());
        System.out.println("Esto se envía al razonador");
    }

}