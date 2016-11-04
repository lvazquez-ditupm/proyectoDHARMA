package utils;

import control.Dharma;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class parses HMM logs and manages the different chains created
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class MarkovController {

    private ArrayList<ArrayList<String>> markovChains;
    private ArrayList<String> markovChain;
    private int markovID;
    private double probMarkov;
    private double done;
    private String attack;

    public MarkovController() {
        markovChains = new ArrayList<>();
    }

    public void parse(Dharma dharma, String markovLog) {

        HashMap<String, Object> markovMap = new HashMap<>();
        markovChain = new ArrayList<>();
        markovLog = markovLog.substring(12);
        probMarkov = Double.parseDouble(markovLog.substring(0, 3));
        markovLog = markovLog.substring(3);
        done = Double.parseDouble(markovLog.substring(0, 3));
        markovLog = markovLog.substring(4);
        attack = markovLog.substring(0, markovLog.indexOf("\""));
        markovLog = markovLog.substring(markovLog.indexOf("\"")+1);
        markovID = Integer.parseInt(markovLog.substring(0, 1));
        markovLog = markovLog.substring(1);

        for (String node : markovLog.split("-")) {

            markovChain.add(node);

            markovMap.put("node", node);
            dharma.processEvent(markovMap, true, markovID, probMarkov, done, attack);
            markovMap.clear();
            //markovChains.add(markovChain);

        }

    }
    
    public void delete(Dharma dharma, String log){
        int id = Integer.parseInt(log.substring(log.indexOf("=")+1));
        System.out.println(log);
        dharma.removeBAG(id);
    }

}
