package core;

import java.util.HashMap;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.ListenableDirectedWeightedGraph;

/**
 * This class generates a JSON according to the BAG
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class JSONGenerator {

    public JSONGenerator() {
    }

    /**
     * Genera el JSON a partir del bag, el historial de nodos, el actual y el
     * futuro (un ataque)
     *
     * @param bag grafo
     * @param selectedNode nodo actual
     * @param nextNode nodo siguiente
     * @param phaseHistory historial de nodos
     * @return string JSON
     */
    public String individualGenerator(ListenableDirectedWeightedGraph<String, DefaultEdge> bag, String selectedNode, String nextNode, ArrayList<String> phaseHistory, int position) {

        Gson gson = new Gson();

        Set<String> nodes = bag.vertexSet();
        HashMap<String, Number> edgeMap;
        HashMap<String, Object> nodeMap;

        ArrayList<HashMap> edgesList = new ArrayList<>();
        ArrayList<HashMap> nodesList = new ArrayList<>();
        ArrayList<String> pathList = new ArrayList<>();

        String pathString = "";
        String nextNodeString = "";

        HashMap<String, Object> jsonMap = new HashMap<>();

        int i = 0;
        int j = 0;

        for (String node : nodes) {

            nodeMap = new HashMap<>();
            nodeMap.put("id", i);
            nodeMap.put("title", node);

            for (int k = 0; k < phaseHistory.size(); k++) {
                if (selectedNode.equals(node)) {
                    nodeMap.put("status", "current");
                    break;
                } else if (nextNode != null && nextNode.equals(node)) {
                    nodeMap.put("status", "next");
                    nextNodeString+=node+"*-";
                    break;
                } else if (phaseHistory.get(k).equals(node) && k < phaseHistory.size() - 1) {
                    nodeMap.put("status", "previous");
                    break;
                } else {//if (k == phaseHistory.size() - 1) {
                    nodeMap.put("status", "none");
                }
            }

            nodesList.add(nodeMap);

            for (String nextCandidate : nodes) {

                if (bag.containsEdge(node, nextCandidate)) {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) bag.getEdge(node, nextCandidate);
                    edgeMap = new HashMap<>();
                    edgeMap.put("source", i);
                    edgeMap.put("target", j);
                    edgeMap.put("weight", bag.getEdgeWeight(e));
                    edgesList.add(edgeMap);
                }
                j++;
            }
            j = 0;
            i++;
        }
        
        for (String phaseHistoryItem : phaseHistory){
            pathString += phaseHistoryItem+"-";
        }
        
        pathString+=nextNodeString;
        
        pathList.add(pathString);

        jsonMap.put("nodes", nodesList);
        jsonMap.put("edges", edgesList);
        jsonMap.put("routes", pathList);
        jsonMap.put("attackID", position+1);

        return gson.toJson(jsonMap);
    }

    /**
     * Genera el JSON a partir del bag, el historial de nodos, el actual y el
     * futuro (todos los ataques simultánteamente)
     *
     * @param bag grafo
     * @param selectedNodes nodos actuales
     * @param nextNodes nodos siguientes
     * @param phaseHistories historiales de nodos
     * @return string JSON
     */
    public String totalGenerator(ListenableDirectedWeightedGraph<String, DefaultEdge> bag, ArrayList<String> selectedNodes, ArrayList<ArrayList<String>> phaseHistories) {

        Gson gson = new Gson();

        Set<String> nodes = bag.vertexSet();
        HashMap<String, Number> edgeMap;
        HashMap<String, Object> nodeMap;

        ArrayList<HashMap> edgesList = new ArrayList<>();
        ArrayList<HashMap> nodesList = new ArrayList<>();
        LinkedHashSet historyList = new LinkedHashSet();

        HashMap<String, ArrayList> jsonMap = new HashMap<>();

        String selectedNode;
        String nextNode;

        String pathString = "";

        int i = 0;
        int j = 0;

        for (String node : nodes) {

            nodeMap = new HashMap<>();
            nodeMap.put("id", i);
            nodeMap.put("title", node);

            String nodeStatus = "";

            for (String currentNode : selectedNodes) {
               
                if (node.equals(currentNode)) {
                    nodeStatus = "current";
                }
            }

            if (nodeStatus.equals("")) {

                for (ArrayList<String> phaseHistory : phaseHistories) {

                    for (String phaseHistoryNode : phaseHistory) {

                        pathString += phaseHistoryNode + "-";

                        if (node.equals(phaseHistoryNode)) {
                            nodeStatus = "previous";
                        }
                    }

                    historyList. add(pathString);
                    pathString = "";
                }
            }

            if (nodeStatus.equals("")) {
                nodeStatus = "none";
            }

            nodeMap.put("status", nodeStatus);
            nodesList.add(nodeMap);

            for (String nextCandidate : nodes) {

                if (bag.containsEdge(node, nextCandidate)) {
                    DefaultWeightedEdge e = (DefaultWeightedEdge) bag.getEdge(node, nextCandidate);
                    edgeMap = new HashMap<>();
                    edgeMap.put("source", i);
                    edgeMap.put("target", j);
                    edgesList.add(edgeMap);
                }
                j++;
            }
            j = 0;
            i++;

            jsonMap.put("nodes", nodesList);
            jsonMap.put("edges", edgesList);
            ArrayList<String> _historyList = new ArrayList<String>(historyList);
            jsonMap.put("routes", _historyList);

        }

        return gson.toJson(jsonMap);
    }
}
