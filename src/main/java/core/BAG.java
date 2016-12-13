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
	private Boolean phantom;
	private String selectedNode;
	private double probMarkov;
	private double done;
	private double risk;
	private String attack;
	private ArrayList<String> phaseHistory;
	private ArrayList<HashMap<String, String>> eventHistoryData;
	private ArrayList<String> markovNodes;
	private int markovID;
	private Calendar startTime;
	private Calendar currentTime;
	private BAG link;
	private final DharmaProperties props = new DharmaProperties();

	/**
	 * Crea un grafo acíclico bayesiano
	 *
	 */
	public BAG() {
		phaseHistory = new ArrayList<>();
		eventHistoryData = new ArrayList<>();
		markovNodes = new ArrayList<>();
		startTime = Calendar.getInstance();
		currentTime = startTime;
		phantom = false;
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
	 * Cambia el peso de un enlace
	 *
	 * @param origin
	 *            Nodo origen
	 * @param destination
	 *            Nodo destino
	 * @param weight
	 *            Nuevo peso
	 */
	public void changeWeigths(String origin, String destination, double weight) {
		bag.setEdgeWeight(bag.getEdge(origin, destination), weight);
	}

	/**
	 * Establece la posición en el grafo
	 *
	 * @param node
	 *            nodo a establecer la posición
	 */
	public void setPosition(String node, int position, ArrayList<BAG> bags, boolean markov, ArrayList<String> nodes,
			double probMarkov, double done, double risk, String attack) throws Exception {
		if (!bag.containsVertex(node)) {
			throw new Exception("Nodo no existente en la red bayesiana");
		}

		if (markov) {
			this.markovNodes.clear();
			this.probMarkov = probMarkov;
			this.done = done;
			this.attack = attack;
			this.risk=risk;
		}
		selectedNode = node;
		phaseHistory.add(selectedNode);
		currentTime = Calendar.getInstance();

		boolean flag = false;

		Iterator<String> it = nodes.iterator();
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
	 * @param node1
	 *            nodo origen
	 * @param node2
	 *            nodo destino
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
	 * Devuelve los nodos a los que se puede llegar desde el nodo introducido
	 * como parámetro
	 *
	 * @param node
	 *            nodo actual
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
	 * Devuelve la probabilidad del enlace
	 *
	 * @param origin
	 *            nodo origen
	 * @param destination
	 *            nodo destino
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
	 * @param event
	 *            evento
	 */
	public void setEventData(HashMap<String, String> event) {
		eventHistoryData.add(event);
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
				done, risk, attack);
		PrintWriter writer = new PrintWriter(
				props.getBagVisualizatorPathValue() + "/public/datos" +position+ ".json", "UTF-8");
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
			if (!bagItem.isPhantom()) {
				selectedNodes.add(bagItem.getPosition());
				phaseHistories.add(bagItem.getHistory());
				markovNodes_.add(bagItem.getMarkovNodes());
				probsMarkov.add(bagItem.getProbMarkov());
				doneList.add(bagItem.getDone());
				attacks.add(bagItem.getAttack());
				ids.add(bagItem.getMarkovID());
			}
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
			bag.exportCompleteJSON(new ArrayList<BAG>());
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static BAG clone(BAG originalBag) {

		BAG newBag = new BAG();
		newBag.setBag(originalBag.getBag());
		newBag.setEventHistoryData(originalBag.getEventHistoryData());
		newBag.setMarkovNodes(originalBag.getMarkovNodes());
		newBag.setSelectedNode(originalBag.getPosition());
		newBag.setPhaseHistory(new ArrayList<String>(originalBag.getPhaseHistory()));

		return newBag;

	}

	public void removeMarkov(ArrayList<String> markovNodes) {
		if (this.markovNodes.equals(markovNodes)) {
			this.markovNodes.clear();
		}
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

	public void setMarkovNodes(ArrayList<String> markovNodes) {
		this.markovNodes = markovNodes;
	}

	public void setMarkovID(int markovID) {
		this.markovID = markovID;
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

	public double getRisk(){
		return risk;
	}
	
	public String getAttack() {
		return attack;
	}
}
