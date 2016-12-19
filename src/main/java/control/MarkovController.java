package control;

import control.Dharma;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * This class parses HMM logs and manages the different chains created
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class MarkovController {

    private ArrayList<Integer> oldQueries = new ArrayList<>();
    private static HashMap<Integer, HashMap<String, Object>> chainMap = new HashMap<>();
    private HashMap<String, Object> data;
    private int markovID;
    private double probMarkov;
    private double done;
    private double risk;
    private String attack;
    private ArrayList<String> nodes;
    private String node;
    private final Dharma dharma = new Dharma();

    public MarkovController() {

    }

    /**
     * Se encarga de parsear el log recibido del HMM y registra el ataque para futuras actualizaciones
     * @param markovLog log del HMM
     */
    public void parse(String markovLog) {

        HashMap<String, Object> markovMap = new HashMap<>();
        data = new HashMap<>();
        nodes = new ArrayList<>();

        markovLog = markovLog.substring(5).replace("\"", "");
        String[] markovLogArray = markovLog.split(";");

        markovID = Integer.parseInt(markovLogArray[0].substring(markovLogArray[0].indexOf("=") + 1));
        attack = markovLogArray[1].substring(markovLogArray[1].indexOf("=") + 1);
        String[] nodesArray = markovLogArray[2].substring(markovLogArray[2].indexOf("=") + 1).split(",");
        for (String nodeItem : nodesArray) {
            nodes.add(nodeItem);
        }
        node = markovLogArray[3].substring(markovLogArray[3].indexOf("=") + 1);
        probMarkov = Double.parseDouble(markovLogArray[4].substring(markovLogArray[4].indexOf("=") + 1));
        probMarkov=Math.round(probMarkov*100)/100d;
        done = Double.parseDouble(markovLogArray[5].substring(markovLogArray[5].indexOf("=") + 1));
        done=Math.round(done*10000)/10000d;
        risk = Double.parseDouble(markovLogArray[6].substring(markovLogArray[6].indexOf("=") + 1));
        risk=Math.round(risk*100)/100d;
        
        data.put("nodes", nodes);
        data.put("node", node);

        if (!chainMap.containsKey(markovID)) {
            boolean flag = true;
            int i = 0;
            while (flag && i < nodesArray.length) {
                if (nodesArray[i].equals(node)) {
                    flag = false;
                }
                markovMap.put("node", nodesArray[i]);
                dharma.processEvent(markovMap, nodes, true, markovID, probMarkov, done, risk, attack);
                i++;
            }
            chainMap.put(markovID, data);
        } else {
            String lastNode = (String) chainMap.get(markovID).get("node");
            if (lastNode.equals(node)) {
                return;
            }
            int i = 0;
            while (!nodesArray[i].equals(lastNode)) {
                i++;
            }

            i++;

            boolean flag = true;
            while (flag) {
                if (nodesArray[i].equals(node)) {
                    flag = false;
                }
                markovMap.put("node", nodesArray[i]);
                dharma.processEvent(markovMap, nodes, true, markovID, probMarkov, done, risk, attack);
                i++;
            }
            chainMap.put(markovID, data);
        }
    }

    /**
     * Ante un ataque finalizado, elimina el ataque y actualiza el grafo
     * @param log log indicando la ID del ataque a borrar
     */
    public void delete(String log) {
        log = log.substring(log.indexOf(":") + 2);
        String[] split = log.split(" ");
        int id = Integer.parseInt(split[0].split("=")[1]);
        int queryID = Integer.parseInt(split[1].split("=")[1]);

        if (!oldQueries.contains(queryID)) {
            oldQueries.add(queryID);
            if (oldQueries.size() > 10) {
                oldQueries.remove(0);
            }
            chainMap.remove(id);
            dharma.removeBAG(id);
        }

    }

}