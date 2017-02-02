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
    private String selectedNode;
    private double probMarkov;
    private double done;
    private HashMap<String, Object> infoAtt;
    private String attack;
    private ArrayList<String> phaseHistory;
    private ArrayList<String> markovNodes;
    private int markovID;
    private final DharmaProperties props = new DharmaProperties();

    /**
     * Crea un grafo acíclico bayesiano
     *
     */
    public Graph() {
        phaseHistory = new ArrayList<>();
        markovNodes = new ArrayList<>();
        graph = new ListenableDirectedWeightedGraph<>(DefaultWeightedEdge.class);
    }

    /**
     * Añade un nodo al grafo
     *
     * @param node nuevo vértice
     * @param previous hashmap de vértices anteriores con sus pesos
     * @param next hashmap de vértices posteriores con sus pesos
     */
    public void addNode(String node, HashMap<String, Double> previous, HashMap<String, Double> next) {
        graph.addVertex(node);
        Iterator it = previous.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            graph.setEdgeWeight(graph.addEdge(e.getKey().toString(), node), (double) e.getValue());
        }
        if (next != null) {
            it = next.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry e = (Map.Entry) it.next();
                graph.setEdgeWeight(graph.addEdge(node, e.getKey().toString()), (double) e.getValue());
            }
        }
    }


    /**
     * Establece la posición en el grafo
     *
     * @param node nodo a establecer la posición
     * @param markovID ID de la cadena que lleva este grafo
     * @param graphs lista con todos los grafos
     * @param nodes nodos relacionados con el ataque
     * @param probMarkov probabilidad de estar en el estado que dice la cadena
     * @param done porcentaje del ataque completado
     * @param infoAtt informacion relacionada con el ataque
     * @param attack nombre del ataque
     */
    public void setPosition(String node, int markovID, ArrayList<Graph> graphs, ArrayList<String> nodes, double probMarkov,
            double done, HashMap<String, Object> infoAtt, String attack) throws Exception {
        if (!graph.containsVertex(node)) {
            throw new Exception("Nodo no existente en la red bayesiana");
        }

        this.phaseHistory = new ArrayList<>();
        this.markovNodes.clear();
        this.probMarkov = probMarkov;
        this.done = done;
        this.attack = attack;
        this.infoAtt = infoAtt;

        selectedNode = node;

        Iterator<String> it = nodes.iterator();
        while (it.hasNext()) {
            String item = it.next();
            this.phaseHistory.add(item);
            if (item.equals(node)) {
                break;
            }
        }

        boolean flag = false;

        it = nodes.iterator();
        while (it.hasNext()) {
            String item = it.next();
            if (flag) {
                this.markovNodes.add(item);
            } else if (item.equals(node)) {
                flag = true;
            }
        }

        try {

            exportIndividualJSON(markovID);
            exportCompleteJSON(graphs);

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * Devuelve los nodos involucrados en el ataque
     *
     * @return lista de nodos
     */
    public ArrayList<String> getMarkovNodes() {
        return markovNodes;
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
     * Genera el grafo a partir de un fichero JSON
     *
     * @param file ruta al fichero
     */
    public void importJSON(String file) throws FileNotFoundException, IOException {

        HashMap<String, Object> parsedGraph;
        Map<String, HashMap> nodes;
        HashSet<String[]> edges;
        JSONGraphParser jgp = new JSONGraphParser();
        String cadena;
        FileReader f = new FileReader(file);
        BufferedReader b = new BufferedReader(f);
        cadena = b.readLine();
        b.close();

        parsedGraph = jgp.parseGraph(cadena);

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
    public void exportIndividualJSON(int position) throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        String json = jsonGen.individualGenerator(graph, selectedNode, phaseHistory, markovNodes, position, probMarkov,
                done, infoAtt, attack);
        PrintWriter writer = new PrintWriter(props.getGraphVisualizatorPathValue() + "/public/datos" + position + ".json",
                "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Exporta el grafo combinado a un fichero JSON
     *
     * @param graphs distintos grafos
     */
    public void exportCompleteJSON(ArrayList<Graph> graphs) throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        ArrayList<String> selectedNodes = new ArrayList<>();
        ArrayList<ArrayList<String>> phaseHistories = new ArrayList<>();
        ArrayList<ArrayList<String>> markovNodes_ = new ArrayList<>();
        ArrayList<Double> probsMarkov = new ArrayList<>();
        ArrayList<Double> doneList = new ArrayList<>();
        ArrayList<String> attacks = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        for (Graph graphItem : graphs) {

            selectedNodes.add(graphItem.getPosition());
            phaseHistories.add(graphItem.getHistory());
            markovNodes_.add(graphItem.getMarkovNodes());
            probsMarkov.add(graphItem.getProbMarkov());
            doneList.add(graphItem.getDone());
            attacks.add(graphItem.getAttack());
            ids.add(graphItem.getMarkovID());

        }

        if (graphs.isEmpty()) {
            try {
                Graph graph_ = new Graph();
                graph_.importJSON(props.getJSONPathValue());
                graph = graph_.getGraph();
            } catch (Exception ex) {
                Logger.getLogger(Graph.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String json = jsonGen.totalGenerator(graph, selectedNodes, markovNodes_, phaseHistories, probsMarkov, doneList,
                attacks, ids);
        PrintWriter writer = new PrintWriter(props.getGraphVisualizatorPathValue() + "/public/datos0.json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Genera un JSON común conun grafo en blanco y borra los demás JSON
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
	 * Otros getters y setters
     */
    public void setGraph(ListenableDirectedWeightedGraph<String, DefaultEdge> graph) {
        this.graph = graph;
    }

    public void setSelectedNode(String selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void setMarkovID(int markovID) {
        this.markovID = markovID;
    }

    public ListenableDirectedGraph<String, DefaultEdge> getGraph() {
        return graph;
    }

    public ArrayList<String> getPhaseHistory() {
        return phaseHistory;
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

    public HashMap<String, Object> getinfoAtt() {
        return infoAtt;
    }

    public String getAttack() {
        return attack;
    }
}
