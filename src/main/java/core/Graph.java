package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgrapht.graph.*;
import control.Dharma;
import utils.DharmaProperties;

/**
 * This class represents the Bayesian Acyclic Graph needed to make an Attack
 * tree.
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Graph {

    private ListenableDirectedGraph<String, DefaultEdge> graph;
    private String currentNode;
    private double probMarkov;
    private double done;
    private HashMap<String, Object> infoAtt;
    private String attack;
    private ArrayList<String> pastNodes;
    private ArrayList<String> futureNodes;
    private int markovID;
    private final DharmaProperties props = new DharmaProperties();
    private BayesNetworkManager bayesNet;

    /**
     * Crea un grafo acíclico bayesiano
     *
     * @param markovID identificador del HMM
     */
    public Graph(int markovID) {
        this.markovID = markovID;
        pastNodes = new ArrayList<>();
        futureNodes = new ArrayList<>();
        graph = new ListenableDirectedGraph<>(DefaultEdge.class);
        try {
            importJSON(props.getJSONPathValue(), markovID);
        } catch (IOException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
        bayesNet = new BayesNetworkManager();
    }

    public Graph() {
        pastNodes = new ArrayList<>();
        futureNodes = new ArrayList<>();
        graph = new ListenableDirectedGraph<>(DefaultEdge.class);
    }

    /**
     * Establece la posición en el grafo
     *
     * @param node nodo a establecer la posición
     * @param id identificador del grafo
     * @param graphs lista con todos los grafos
     * @param nodes nodos que pertenecen al ataque mostrado en el grafo
     * @param probMarkov probabilidad de pertenecer al estado actual del ataque
     * @param done porcentaje del ataque completado
     * @param infoAtt infotmación sobre el ataque
     * @param attack nombre del ataque
     */
    public void setPosition(String node, int id, ArrayList<Graph> graphs, ArrayList<String> nodes, double probMarkov,
            double done, HashMap<String, Object> infoAtt, String attack) throws Exception {
        if (!graph.containsVertex(node)) {
            throw new Exception("Nodo no existente en la red bayesiana");
        }

        this.pastNodes = new ArrayList<>();
        this.futureNodes.clear();
        this.probMarkov = probMarkov;
        this.done = done;
        this.attack = attack;
        this.infoAtt = infoAtt;

        currentNode = node;

        Iterator<String> it = nodes.iterator();
        while (it.hasNext()) {
            String item = it.next();
            this.pastNodes.add(item);
            bayesNet.eventOcurred(item);
            if (item.equals(node)) {
                break;
            }
        }

        boolean flag = false;

        it = nodes.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (flag) {
                this.futureNodes.add(item);
            } else if (item.equals(node)) {
                flag = true;
            }
        }

        try {

            exportIndividualJSON(id);
            exportCompleteJSON(graphs);

        } catch (Exception ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Devuelve el estado del nodo
     *
     * @param node nodo
     * @return estado
     */
    public String getNodeStatus(String node) {
        if (node.equals(this.getCurrentNode())) {
            return "current";
        } else if (getFutureNodes().contains(node)) {
            return "next";
        } else if (this.getPhaseHistory().contains(node)) {
            return "previous";
        } else {
            return "none";
        }
    }

    /**
     * Genera el grafo a partir de un fichero JSON
     *
     * @param file ruta al fichero
     * @param hmmId identificador del grafo
     */
    public void importJSON(String file, int hmmId) throws FileNotFoundException, IOException {

        HashMap<String, Object> parsedGraph;
        Map<String, HashMap> nodes;
        HashSet<String[]> edges;
        JSONGraphParser jgp = new JSONGraphParser();
        String cadena;
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);
        cadena = b.readLine();
        b.close();

        parsedGraph = jgp.parseGraph(cadena, hmmId);

        nodes = (Map<String, HashMap>) parsedGraph.get("nodes");
        edges = (HashSet<String[]>) parsedGraph.get("edges");

        for (String node : nodes.values().toArray(new String[0])) {
            graph.addVertex(node);
        }

        for (String[] edge : edges) {
            graph.addEdge(edge[0], edge[1]);
        }
    }

    /**
     * Exporta el grafo de un ataque a un fichero JSON
     *
     * @param position ID del grafo
     */
    public void exportIndividualJSON(int position)
            throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        String json = jsonGen.individualGenerator(graph, bayesNet, currentNode, pastNodes,
                futureNodes, position, probMarkov, done, infoAtt, attack);
        PrintWriter writer = new PrintWriter(props.getGraphVisualizatorPathValue()
                + "/public/datos" + position + ".json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Exporta el grafo combinado a un fichero JSON
     *
     * @param graphs distintos grafos
     */
    public void exportCompleteJSON(ArrayList<Graph> graphs)
            throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        ArrayList<String> selectedNodes = new ArrayList<>();
        ArrayList<ArrayList<String>> phaseHistories = new ArrayList<>();
        ArrayList<ArrayList<String>> markovNodes_ = new ArrayList<>();
        ArrayList<Double> probsMarkov = new ArrayList<>();
        ArrayList<Double> doneList = new ArrayList<>();
        ArrayList<String> attacks = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        for (Graph graphItem : graphs) {
            selectedNodes.add(graphItem.getCurrentNode());
            phaseHistories.add(graphItem.getPastNodes());
            markovNodes_.add(graphItem.getFutureNodes());
            probsMarkov.add(graphItem.getProbMarkov());
            doneList.add(graphItem.getDone());
            attacks.add(graphItem.getAttack());
            ids.add(graphItem.getMarkovID());
        }

        if (graphs.isEmpty()) {
            try {
                Graph graph_ = new Graph();
                graph_.importJSON(props.getJSONPathValue(), markovID);
                graph = graph_.getGraph();
            } catch (Exception ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String json = jsonGen.totalGenerator(graph, selectedNodes, markovNodes_, phaseHistories,
                probsMarkov, doneList, attacks, ids);
        PrintWriter writer = new PrintWriter(props.getGraphVisualizatorPathValue()
                + "/public/datos0.json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Genera un grafo vacío
     */
    public static void exportCleanJSON() {
        try {
            Graph graph = new Graph();
            Dharma.deleteFolder();
            graph.exportCompleteJSON(new ArrayList<Graph>());
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Otros getters y setters
     */
    public ListenableDirectedGraph<String, DefaultEdge> getGraph() {
        return graph;
    }

    public ArrayList<String> getPhaseHistory() {
        return pastNodes;
    }

    public int getMarkovID() {
        return markovID;
    }

    public double getProbMarkov() {
        return probMarkov;
    }

    public double getDone() {
        return done;
    }

    public String getAttack() {
        return attack;
    }

    public ArrayList<String> getPastNodes() {
        return pastNodes;
    }

    public String getCurrentNode() {
        return currentNode;
    }

    public ArrayList<String> getFutureNodes() {
        return futureNodes;
    }
}
