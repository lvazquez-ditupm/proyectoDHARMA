package core;

import java.util.HashMap;
import java.util.Map;
import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import smile.Network;

/**
 * This class makes a parsing of the JSON exported by the d3js graph generator
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class JSONGraphParser {

    /**
     * Parsea el JSON para obtener los nodos y enlaces
     *
     * @param json texto a parsear
     * @param hmmId identificador del HMM
     * @return nodos y enlaces
     */
    public HashMap<String, Object> parseGraph(String json, int hmmId) {
        Network net = new Network();

        HashMap<String, String> exportedNodes = new HashMap<>();
        HashSet<String[]> exportedEdges = new HashSet<>();
        HashMap<String, Object> parsedGraph = new HashMap<>();

        Map jsonMap = new Gson().fromJson(json, Map.class);
        String[] nodes = jsonMap.get("nodes").toString().replace("[", " ").
                replace("}]", "").replace(" {", "").split("},");
        String[] edges = jsonMap.get("edges").toString().replace("[", " ").
                replace("}]", "").replace(" {", "").split("},");

        for (String node : nodes) {

            String name = "";
            String id = "";

            for (String nodeElement : node.split(", ")) {
                String[] item = nodeElement.split("=");
                if ("title".equals(item[0])) {
                    name = item[1];
                } else if ("id".equals(item[0])) {
                    id = item[1];
                }
            }

            exportedNodes.put(id, name);
            net.addNode(Network.NodeType.Cpt, adjustName(name));
            net.setOutcomeId(adjustName(name), 0, "False");
            net.setOutcomeId(adjustName(name), 1, "True");

        }

        //System.out.println("Grafo generado: ");
        for (String edge : edges) {

            String source = "";
            String target = "";

            for (String edgeElement : edge.split(", ")) {
                String[] item = edgeElement.split("=");
                if ("source".equals(item[0])) {
                    source = exportedNodes.get(item[1]);
                } else if ("target".equals(item[0])) {
                    target = exportedNodes.get(item[1]);
                }
            }

            exportedEdges.add(new String[]{source, target});
            net.addArc(adjustName(source), adjustName(target));
        }

        parsedGraph.put("nodes", exportedNodes);
        parsedGraph.put("edges", exportedEdges);

        if (!new File("bayesNet.xdsl").exists()) {
            net.writeFile("bayesNet.xdsl");
        }
        if (!new File("bayesNet.txt").exists()) {
            Writer output = null;
            try {
                String[] nodesArray = net.getAllNodeIds();
                String newLine = "";
                for (String node : nodesArray) {
                    newLine += node + ",";
                }
                newLine = newLine.substring(0, newLine.length() - 1);
                newLine += "\n";
                output = new BufferedWriter(new FileWriter("./bayesNet.txt", true));
                output.append(newLine);
                output.close();
            } catch (IOException ex) {
                Logger.getLogger(JSONGraphParser.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    output.close();
                } catch (IOException ex) {
                    Logger.getLogger(JSONGraphParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return parsedGraph;

    }

    /**
     * Cambia los espacios por barras bajas en los nombres de los nodos
     *
     * @param name nombre del nodo
     */
    private String adjustName(String name) {
        if (name.contains(" ")) {
            name = name.replace(" ", "_");
        }
        return name;
    }
}
