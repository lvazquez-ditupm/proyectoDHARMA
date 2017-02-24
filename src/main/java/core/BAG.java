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
public class BAG {

    private ListenableDirectedGraph<String, DefaultEdge> bag;
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
     */
    public BAG(int markovID) {
        this.markovID = markovID;
        pastNodes = new ArrayList<>();
        futureNodes = new ArrayList<>();
        bag = new ListenableDirectedGraph<>(DefaultEdge.class);
        try {
            importJSON(props.getJSONPathValue(), markovID);
        } catch (IOException ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
        }
        bayesNet = new BayesNetworkManager(markovID);
    }

    public BAG() {
        pastNodes = new ArrayList<>();
        futureNodes = new ArrayList<>();
        bag = new ListenableDirectedGraph<>(DefaultEdge.class);
    }

    /**
     * Establece la posición en el grafo
     *
     * @param node nodo a establecer la posición
     */
    public void setPosition(String node, int id, ArrayList<BAG> bags, ArrayList<String> nodes, double probMarkov,
            double done, HashMap<String, Object> infoAtt, String attack) throws Exception {
        if (!bag.containsVertex(node)) {
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
        
        bayesNet.updateProbs(id);
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
            exportCompleteJSON(bags);

        } catch (Exception ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
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
     * @param hmmId identificador del BAG
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
            bag.addVertex(node);
        }

        for (String[] edge : edges) {
            bag.addEdge(edge[0], edge[1]);
        }
    }

    /**
     * Exporta el grafo de un ataque a un fichero JSON
     *
     * @param position ID del BAG
     */
    public void exportIndividualJSON(int position)
            throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        String json = jsonGen.individualGenerator(bag, bayesNet, currentNode, pastNodes,
                futureNodes, position, probMarkov, done, infoAtt, attack);
        PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue()
                + "/public/datos" + position + ".json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    /**
     * Exporta el grafo combinado a un fichero JSON
     *
     * @param bags distintos BAG
     */
    public void exportCompleteJSON(ArrayList<BAG> bags)
            throws FileNotFoundException, UnsupportedEncodingException {
        JSONGenerator jsonGen = new JSONGenerator();
        ArrayList<String> selectedNodes = new ArrayList<>();
        ArrayList<ArrayList<String>> phaseHistories = new ArrayList<>();
        ArrayList<ArrayList<String>> markovNodes_ = new ArrayList<>();
        ArrayList<Double> probsMarkov = new ArrayList<>();
        ArrayList<Double> doneList = new ArrayList<>();
        ArrayList<String> attacks = new ArrayList<>();
        ArrayList<Integer> ids = new ArrayList<>();

        for (BAG bagItem : bags) {

            selectedNodes.add(bagItem.getCurrentNode());
            phaseHistories.add(bagItem.getPastNodes());
            markovNodes_.add(bagItem.getFutureNodes());
            probsMarkov.add(bagItem.getProbMarkov());
            doneList.add(bagItem.getDone());
            attacks.add(bagItem.getAttack());
            ids.add(bagItem.getMarkovID());

        }

        if (bags.isEmpty()) {
            try {
                BAG bag_ = new BAG();
                bag_.importJSON(props.getJSONPathValue(), markovID);
                bag = bag_.getBag();
            } catch (Exception ex) {
                Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        String json = jsonGen.totalGenerator(bag, selectedNodes, markovNodes_, phaseHistories,
                probsMarkov, doneList, attacks, ids);
        PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue()
                + "/public/datos0.json", "UTF-8");
        writer.println(json);
        writer.close();
    }

    public static void exportCleanJSON() {
        try {
            BAG bag = new BAG();
            Dharma.deleteFolder();
            bag.exportCompleteJSON(new ArrayList<BAG>());
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    Otros getters y setters
     */
    public ListenableDirectedGraph<String, DefaultEdge> getBag() {
        return bag;
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
