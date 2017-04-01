package control;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class parses HMM logs and manages the different chains created
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class MarkovController {

    private HashMap<String, Object> data;
    private int markovID;
    private double probMarkov;
    private double done;
    private double risk;
    private String attack;
    private ArrayList<String> nodes;
    private String node;
    private final Dharma dharma = new Dharma();
    private String ipSrc = "";
    private String ipDst = "";
    private int port = 0;
    private String timestamp = "";
    HashMap<String, Object> infoAtt = new HashMap<>();

    /**
     * Se encarga de parsear el log recibido del HMM y registra el ataque para
     * futuras actualizaciones
     *
     * @param markovLog log del HMM
     */
    public void parse(String markovLog) {

        HashMap<String, Object> markovMap = new HashMap<>();
        data = new HashMap<>();
        nodes = new ArrayList<>();

        markovLog = markovLog.substring(5).replace("\"", "");
        String[] markovLogArray = markovLog.split(";");

        attack = markovLogArray[1].substring(markovLogArray[1].indexOf("=") + 1);
        String[] nodesArray = markovLogArray[2].substring(markovLogArray[2].indexOf("=") + 1).split(",");
        for (String nodeItem : nodesArray) {
            nodes.add(nodeItem);
        }
        node = markovLogArray[3].substring(markovLogArray[3].indexOf("=") + 1);
        probMarkov = Double.parseDouble(markovLogArray[4].substring(markovLogArray[4].indexOf("=") + 1));
        probMarkov = Math.round(probMarkov * 100) / 100d;
        done = Double.parseDouble(markovLogArray[5].substring(markovLogArray[5].indexOf("=") + 1));
        done = Math.round(done * 10000) / 10000d;
        risk = Double.parseDouble(markovLogArray[6].substring(markovLogArray[6].indexOf("=") + 1));
        risk = Math.round(risk * 100) / 100d;
        infoAtt.put("risk", risk);
        markovID = Integer.parseInt(markovLogArray[7].substring(markovLogArray[7].indexOf("=") + 1));
        for (int i = 8; i <= 11; i++) {
            if (markovLogArray.length > i + 1) {
                switch (markovLogArray[i].substring(0, markovLogArray[i].indexOf("="))) {
                    case "IPsrc":
                        ipSrc = markovLogArray[i].substring(markovLogArray[i].indexOf("=") + 1);
                        infoAtt.put("IPsrc", ipSrc);
                        break;
                    case "IPdst":
                        ipDst = markovLogArray[i].substring(markovLogArray[i].indexOf("=") + 1);
                        infoAtt.put("IPdst", ipDst);
                        break;
                    case "Port":
                        port = Integer.parseInt(markovLogArray[i].substring(markovLogArray[i].indexOf("=") + 1));
                        infoAtt.put("Port", port);
                        break;
                    case "Timestamp":
                        timestamp = markovLogArray[i].substring(markovLogArray[i].indexOf("=") + 1);
                        infoAtt.put("Timestamp", timestamp);
                        break;
                }
                continue;
            }
            break;
        }

        data.put("nodes", nodes);
        data.put("node", node);
        data.put("prob", probMarkov);
        data.put("done", done);
        markovMap.put("node", node);
        dharma.processEvent(markovMap, nodes, markovID, probMarkov, done, infoAtt, attack);
    }

    /**
     * Ante un ataque finalizado, elimina el ataque y actualiza el grafo
     *
     * @param log log indicando la ID del ataque a borrar
     */
    public void delete(String log) {
        log = log.substring(log.indexOf(":") + 2);
        int id = Integer.parseInt(log.split("=")[1]);
        //System.out.println("Eliminado ataque "+id);
        dharma.removeGraph(id);

    }

}
