package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.graph.*;
import utils.PropsUtil;

/**
 * This class represents the Bayesian Acyclic Graph needed to make an Attack
 * tree.
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class BAG {

    private ListenableDirectedWeightedGraph<String, DefaultEdge> bag;
    private Boolean phantom;
    private String selectedNode;
    private String nextNode;
    private ArrayList<String> phaseHistory;
    private ArrayList<HashMap<String, String>> eventHistoryData;
    private Calendar startTime;
    private Calendar currentTime;
    private BAG link;
    private final PropsUtil props = new PropsUtil();

    /**
     * Crea un grafo acíclico bayesiano
     *
     */
    public BAG() {
        phaseHistory = new ArrayList<>();
        eventHistoryData = new ArrayList<>();
        startTime = Calendar.getInstance();
        currentTime = startTime;
        phantom = false;
        bag = new ListenableDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Añade un nodo al grafo
     *
     * @param node nuevo vértice
     * @param previous hashmap de vértices anteriores con sus pesos
     * @param next hashmap de vértices posteriores con sus pesos
     */
    public void addNode(String node, HashMap<String, Double> previous, HashMap<String, Double> next) {
        bag.addVertex(node);
        Iterator it = previous.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            bag.setEdgeWeight(bag.addEdge(e.getKey().toString(), node), (double) e.getValue());
        }
        if (next != null) {
            it = next.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                bag.setEdgeWeight(bag.addEdge(node, e.getKey().toString()), (double) e.getValue());
            }
        }
    }

    /**
     * Elimina un nodo del grafo
     *
     * @param node Nodo a eliminar
     */
    public void deleteNode(String node) {
        bag.removeVertex(node);
    }

    /**
     * Cambia el peso de un enlace
     *
     * @param origin Nodo origen
     * @param destination Nodo destino
     * @param weight Nuevo peso
     */
    public void changeWeigths(String origin, String destination, double weight) {
        bag.setEdgeWeight(bag.getEdge(origin, destination), weight);
    }

    /**
     * Establece la posición en el grafo
     *
     * @param node nodo a establecer la posición
     */
    public void setPosition(String node, int position, ArrayList<BAG> bags) throws Exception {
        if (!bag.containsVertex(node)) {
            throw new Exception("Nodo no existente en la red bayesiana");
        }
        selectedNode = node;
        phaseHistory.add(selectedNode);
        currentTime = Calendar.getInstance();
        try {
            if (!this.isPhantom()) {
                exportIndividualJSON(position);
                exportCompleteJSON(bags);
            }
        } catch (Exception ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Establece el BAG como oculto
     */
    public void setPhantom(BAG bagLink) {
        phantom = true;
        link = bagLink;
    }

    /**
     * Establece el BAG como visible
     */
    public void setReal() {
        phantom = false;
        link = null;
    }

    /**
     * Devuelve true si la red está oculta
     */
    public boolean isPhantom() {
        return phantom;
    }

    /**
     * Devuelve el número de enlaces que salen de un nodo
     *
     * @param node
     * @return
     */
    public int numberOfEdges(String node) {
        return bag.outgoingEdgesOf(node).size();
    }

    /**
     * Devuelve un booleano indicando si dos nodos están conectados en el BAG
     *
     * @param node1 nodo origen
     * @param node2 nodo destino
     * @return conexión
     */
    public boolean connected(String node1, String node2) {
        return bag.containsEdge(node1, node2);
    }

    /**
     * Devuelve el último nodo del BAG
     *
     * @return último nodo
     */
    public String getLastNode() {
        if (phaseHistory.size() >= 1) {
            return phaseHistory.get(phaseHistory.size() - 1);
        } else {
            return null;
        }

    }

    /**
     * Devuelve el estado del nodo
     *
     * @param node nodo
     * @return estado
     */
    public String getNodeStatus(String node) {
        if (node.equals(this.getPosition())) {
            return "current";
        } else if (node.equals(this.getNext())) {
            return "next";
        } else if (this.getPhaseHistory().contains(node)) {
            return "previous";
        } else {
            return "none";
        }
    }

    /**
     * Devuelve los nodos a los que se puede llegar desde el nodo introducido
     * como parámetro
     *
     * @param node nodo actual
     * @return lista de nodos candidatos a ser el siguiente paso del ataque
     */
    public HashMap<String, Double> getNextCandidates(String node) {
        Set<String> nodes = bag.vertexSet();
        HashMap<String, Double> candidates = new HashMap<>();
        for (String nextCandidate : nodes) {
            if (bag.containsEdge(node, nextCandidate)) {
                DefaultWeightedEdge e = (DefaultWeightedEdge) bag.getEdge(node, nextCandidate);
                candidates.put(nextCandidate, bag.getEdgeWeight(e));
            }
        }
        return candidates;
    }

    /**
     * Devuelve el nodo siguiente
     *
     * @return nombre del nodo
     */
    public String getNext() {
        return nextNode;
    }

    /**
     * Devuelve el nodo actual
     *
     * @return nombre del nodo
     */
    public String getPosition() {
        return selectedNode;
    }

    /**
     * Devuelve la lista de nodos previos
     *
     * @return lista de nodos previos
     */
    public ArrayList<String> getHistory() {
        return phaseHistory;
    }

    /**
     * Devuelve la probabilidad del enlace
     *
     * @param origin nodo origen
     * @param destination nodo destino
     * @return probabilidad del enlace
     */
    public double getWeight(String origin, String destination) {
        return bag.getEdgeWeight(bag.getEdge(origin, destination));
    }

    /**
     * Devuelve la duración del ataque
     *
     * @return duración del ataque
     */
    public double getTime() {

        return currentTime.getTimeInMillis() - startTime.getTimeInMillis();

    }

    /**
     * Devuelve el grafo al que está asociado el grafo oculto
     */
    public BAG getLink() {
        return link;
    }

    /**
     * Añade un evento al historial del BAG
     *
     * @param event evento
     */
    public void setEventData(HashMap<String, String> event) {
        eventHistoryData.add(event);
    }

    /**
     * Establece el nodo hacia el que va a evolucionar el ataque
     *
     * @param next nodo siguiente
     */
    public void setNext(String next, int position) {
        nextNode = next;
        try {
            exportIndividualJSON(position);
        } catch (Exception ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Elimina todos los nodos posteriores al dado, convirtiéndose este en el
     * actual
     *
     * @param node nodo actual
     */
    public void deleteFrom(String node) {

        selectedNode = node;
        nextNode = null;

        boolean deleteFlag = false;

        for (int i = 0; i < phaseHistory.size(); i++) {
            if (deleteFlag) {
                phaseHistory.remove(i);
            } else if (phaseHistory.get(i).equals(node)) {
                deleteFlag = true;
            }
        }

    }

    /**
     * Genera el grafo a partir de un fichero JSON
     *
     * @param file ruta al fichero
     */
    public void importJSON(String file) throws FileNotFoundException, IOException {

        HashMap<String, Object> parsedGraph;
        Map<String, HashMap> nodes;
        HashSet<String[]> edges;
        Map<String, Double> weights;
        JSONGraphParser jgp = new JSONGraphParser();
        String cadena;
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);
        cadena = b.readLine();
        b.close();

        parsedGraph = jgp.parseGraph(cadena);

        nodes = (Map<String, HashMap>) parsedGraph.get("nodes");
        edges = (HashSet<String[]>) parsedGraph.get("edges");
        weights = (Map<String, Double>) parsedGraph.get("weights");

        for (String node : nodes.values().toArray(new String[0])) {
            bag.addVertex(node);
        }

        for (String[] edge : edges) {
            bag.setEdgeWeight(bag.addEdge(edge[0], edge[1]), weights.get(edge[0] + "-" + edge[1]));
        }
    }

    /**
     * Exporta el grafo de un ataque a un fichero JSON
     *
     * @param filePath ruta a fichero
     */
    public void exportIndividualJSON(int position) throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        String json = jsonGen.individualGenerator(bag, selectedNode, nextNode, phaseHistory, position);
        PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue() + "/public/datos" + (position + 1) + ".json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Exporta el grafo completo a un fichero JSON
     *
     * @param filePath ruta a fichero
     */
    public void exportCompleteJSON(ArrayList<BAG> bags) throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        ArrayList<String> selectedNodes = new ArrayList<>();
        ArrayList<String> nextNodes = new ArrayList<>();
        ArrayList<ArrayList<String>> phaseHistories = new ArrayList<>();

        for (BAG bagItem : bags) {
            if (!bagItem.isPhantom()) {
                selectedNodes.add(bagItem.getPosition());
                nextNodes.add(bagItem.getNext());
                phaseHistories.add(bagItem.getHistory());
            }
        }

        String json = jsonGen.totalGenerator(bag, selectedNodes, phaseHistories);
        PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue() + "/public/datos0.json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    public static BAG clone(BAG originalBag) {

        BAG newBag = new BAG();
        newBag.setBag(originalBag.getBag());
        newBag.setEventHistoryData(originalBag.getEventHistoryData());
        newBag.setNextNode(originalBag.getNext());
        newBag.setSelectedNode(originalBag.getPosition());
        newBag.setPhaseHistory(new ArrayList<String>(originalBag.getPhaseHistory()));

        return newBag;

    }

    /*
     * Otros getters y setters
     */
    public ListenableDirectedWeightedGraph<String, DefaultEdge> getBag() {
        return bag;
    }

    public void setBag(ListenableDirectedWeightedGraph<String, DefaultEdge> bag) {
        this.bag = bag;
    }

    public void setSelectedNode(String selectedNode) {
        this.selectedNode = selectedNode;
    }

    public ArrayList<String> getPhaseHistory() {
        return phaseHistory;
    }

    public void setPhaseHistory(ArrayList<String> phaseHistory) {
        this.phaseHistory = phaseHistory;
    }

    public ArrayList<HashMap<String, String>> getEventHistoryData() {
        return eventHistoryData;
    }

    public void setEventHistoryData(ArrayList<HashMap<String, String>> eventHistoryData) {
        this.eventHistoryData = eventHistoryData;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getCurrentTime() {
        return currentTime;
    }

    public void setNextNode(String nextNode) {
        this.nextNode = nextNode;
    }
}
