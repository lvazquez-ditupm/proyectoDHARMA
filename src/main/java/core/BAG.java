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

	private ListenableDirectedWeightedGraph<String, DefaultEdge> bag;
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
	public BAG() {
		phaseHistory = new ArrayList<>();
		markovNodes = new ArrayList<>();
		bag = new ListenableDirectedWeightedGraph<>(DefaultWeightedEdge.class);
	}

	/**
	 * Añade un nodo al grafo
	 *
	 * @param node
	 *            nuevo vértice
	 * @param previous
	 *            hashmap de vértices anteriores con sus pesos
	 * @param next
	 *            hashmap de vértices posteriores con sus pesos
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
	 * @param node
	 *            Nodo a eliminar
	 */
	public void deleteNode(String node) {
		bag.removeVertex(node);
	}

	/**
	 * Establece la posición en el grafo
	 *
	 * @param node
	 *            nodo a establecer la posición
	 */
	public void setPosition(String node, int position, ArrayList<BAG> bags, ArrayList<String> nodes, double probMarkov,
			double done, HashMap<String, Object> infoAtt, String attack) throws Exception {
		if (!bag.containsVertex(node)) {
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
			} else {
				continue;
			}
		}

		try {

			exportIndividualJSON(position);
			exportCompleteJSON(bags);

		} catch (Exception ex) {
			Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	/**
	 * Devuelve el estado del nodo
	 *
	 * @param node
	 *            nodo
	 * @return estado
	 */
	public String getNodeStatus(String node) {
		if (node.equals(this.getPosition())) {
			return "current";
		} else if (getMarkovNodes().contains(node)) {
			return "next";
		} else if (this.getPhaseHistory().contains(node)) {
			return "previous";
		} else {
			return "none";
		}
	}

	/**
	 * Devuelve los nodos previstos por el HMM
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
	 * Elimina todos los nodos posteriores al dado, convirtiéndose este en el
	 * actual
	 *
	 * @param node
	 *            nodo actual
	 */
	public void deleteFrom(String node) {

		selectedNode = node;

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
	 * @param file
	 *            ruta al fichero
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
	 * @param position
	 *            ID del BAG
	 */
	public void exportIndividualJSON(int position) throws FileNotFoundException, UnsupportedEncodingException {
		JSONGenerator jsonGen = new JSONGenerator();
		String json = jsonGen.individualGenerator(bag, selectedNode, phaseHistory, markovNodes, position, probMarkov,
				done, infoAtt, attack);
		PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue() + "/public/datos" + position + ".json",
				"UTF-8");
		writer.println(json);
		writer.close();
	}

	/**
	 * Exporta el grafo combinado a un fichero JSON
	 *
	 * @param bags
	 *            distintos BAG
	 */
	public void exportCompleteJSON(ArrayList<BAG> bags) throws FileNotFoundException, UnsupportedEncodingException {
		JSONGenerator jsonGen = new JSONGenerator();
		ArrayList<String> selectedNodes = new ArrayList<>();
		ArrayList<ArrayList<String>> phaseHistories = new ArrayList<>();
		ArrayList<ArrayList<String>> markovNodes_ = new ArrayList<>();
		ArrayList<Double> probsMarkov = new ArrayList<>();
		ArrayList<Double> doneList = new ArrayList<>();
		ArrayList<String> attacks = new ArrayList<>();
		ArrayList<Integer> ids = new ArrayList<>();

		for (BAG bagItem : bags) {

			selectedNodes.add(bagItem.getPosition());
			phaseHistories.add(bagItem.getHistory());
			markovNodes_.add(bagItem.getMarkovNodes());
			probsMarkov.add(bagItem.getProbMarkov());
			doneList.add(bagItem.getDone());
			attacks.add(bagItem.getAttack());
			ids.add(bagItem.getMarkovID());

		}

		if (bags.isEmpty()) {
			try {
				BAG bag_ = new BAG();
				bag_.importJSON(props.getJSONPathValue());
				bag = bag_.getBag();
			} catch (Exception ex) {
				Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		String json = jsonGen.totalGenerator(bag, selectedNodes, markovNodes_, phaseHistories, probsMarkov, doneList,
				attacks, ids);
		PrintWriter writer = new PrintWriter(props.getBagVisualizatorPathValue() + "/public/datos0.json", "UTF-8");
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
	 * Otros getters y setters
	 */

	public void setBag(ListenableDirectedWeightedGraph<String, DefaultEdge> bag) {
		this.bag = bag;
	}

	public void setSelectedNode(String selectedNode) {
		this.selectedNode = selectedNode;
	}

	public void setMarkovID(int markovID) {
		this.markovID = markovID;
	}

	public ListenableDirectedWeightedGraph<String, DefaultEdge> getBag() {
		return bag;
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
