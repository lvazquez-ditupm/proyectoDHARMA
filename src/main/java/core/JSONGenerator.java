package core;

import java.util.HashMap;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
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
	 * @param bag
	 *            grafo
	 * @param selectedNode
	 *            nodo actual
	 * @param phaseHistory
	 *            historial de nodos
	 * @param markovNodes
	 *            nodos siguientes previstos por HMM
	 * @param position
	 *            ID del ataque
	 * @param probMarkov
	 *            probabilidad de estar en el punto actual del HMM
	 * @param done
	 *            porcentaje realizado del ataque
	 * @param attack
	 *            tipo de ataque
	 * @return string JSON
	 */
	public String individualGenerator(ListenableDirectedWeightedGraph<String, DefaultEdge> bag, String selectedNode,
			ArrayList<String> phaseHistory, ArrayList<String> markovNodes, int position, double probMarkov, double done, double risk,
			String attack) {

		Gson gson = new Gson();

		Set<String> nodes = bag.vertexSet();
		HashMap<String, Number> edgeMap;
		HashMap<String, Object> nodeMap;

		ArrayList<HashMap> edgesList = new ArrayList<>();
		ArrayList<HashMap> nodesList = new ArrayList<>();
		ArrayList<String> pathList = new ArrayList<>();

		String pathString = "";

		HashMap<String, Object> jsonMap = new HashMap<>();

		int i = 0;
		int j = 0;

		for (String node : nodes) {

			nodeMap = new HashMap<>();
			nodeMap.put("id", i);
			nodeMap.put("title", node);

			if (selectedNode != null && selectedNode.equals(node)) {
				nodeMap.put("status", "current" + probMarkov);

			}

			for (int k = 0; k < phaseHistory.size(); k++) {
				if (phaseHistory.get(k).equals(node) && k < phaseHistory.size() - 1) {
					nodeMap.put("status", "previous");
					break;
				}
			}

			for (int k = 0; k < markovNodes.size(); k++) {
				if (nodeMap.get("status") == null && markovNodes.get(k).equals(node)) {
					nodeMap.put("status", "markov");
					break;
				}
			}

			if (nodeMap.get("status") == null && !markovNodes.contains(node)) {
				nodeMap.put("status", "none");
			}

			nodesList.add(nodeMap);

			for (String nextCandidate : nodes) {

				if (bag.containsEdge(node, nextCandidate)) {
					DefaultWeightedEdge e = (DefaultWeightedEdge) bag.getEdge(node, nextCandidate);
					edgeMap = new HashMap<>();
					edgeMap.put("source", i);
					edgeMap.put("target", j);
					// edgeMap.put("weight", bag.getEdgeWeight(e)); DESCOMENTAR
					// PARA MOSTRAR LOS PESOS DE LOS ENLACES
					edgesList.add(edgeMap);
				}
				j++;
			}
			j = 0;
			i++;

		}

		for (i = 0; i < phaseHistory.size(); i++) {
			pathString += phaseHistory.get(i) + "-";
		}

		for (j = 0; j < markovNodes.size(); j++) {
			pathString += markovNodes.get(j) + "*-";
		}

		pathList.add(pathString);

		jsonMap.put("nodes", nodesList);
		jsonMap.put("edges", edgesList);
		jsonMap.put("routes", pathList);
		jsonMap.put("attackID", position + 1);
		jsonMap.put("done", done);
		jsonMap.put("risk", risk);
		jsonMap.put("attack", attack);

		return gson.toJson(jsonMap);
	}

	/**
	 * Genera el JSON a partir del bag, el historial de nodos, el actual y el
	 * futuro (todos los ataques simultánteamente)
	 *
	 * @param bag
	 *            grafo
	 * @param selectedNodes
	 *            nodos actuales
	 * @param nextNodes
	 *            nodos siguientes
	 * @param probsMarkov
	 *            probabilidades de estar en el punto actual del HMM
	 * @param phaseHistories
	 *            historiales de nodos
	 * @param doneList
	 *            lista de porcentajes de éxito en ataques
	 * @param attacks
	 *            lista de tipos de ataques
	 * @return string JSON
	 */
	public String totalGenerator(ListenableDirectedWeightedGraph<String, DefaultEdge> bag,
			ArrayList<String> selectedNodes, ArrayList<ArrayList<String>> nextNodes,
			ArrayList<ArrayList<String>> phaseHistories, ArrayList<Double> probsMarkov, ArrayList<Double> doneList,
			ArrayList<String> attacks, ArrayList<Integer> ids) {

		Gson gson = new Gson();

		Set<String> nodes = bag.vertexSet();
		HashMap<String, Number> edgeMap;
		HashMap<String, Object> nodeMap;

		ArrayList<HashMap> edgesList = new ArrayList<>();
		ArrayList<HashMap> nodesList = new ArrayList<>();
		LinkedHashSet historyList = new LinkedHashSet();
		
		String pathString;

		HashMap<String, ArrayList> jsonMap = new HashMap<>();

		int i = 0;
		int j = 0;

		for (String node : nodes) {

			boolean flag = false;

			nodeMap = new HashMap<>();
			nodeMap.put("id", i);
			nodeMap.put("title", node);

			if (node.equals("A2")) {
				System.out.println("");
			}

			for (int k = 0; k < selectedNodes.size(); k++) {
				if (nodeMap.get("status") == null) {
					if (node.equals(selectedNodes.get(k))) {
						nodeMap.put("status", "current" + probsMarkov.get(k));
						nodesList.add(nodeMap);
					} else if (nextNodes.get(k).contains(node)) {
						nodeMap.put("status", "markov");
						nodesList.add(nodeMap);
					} else if (phaseHistories.get(k).contains(node)) {
						nodeMap.put("status", "previous");
						nodesList.add(nodeMap);
					}
				} else if (nodeMap.get("status").toString().contains("current") && node.equals(selectedNodes.get(k))) {
					String oldValue_ = nodeMap.get("status").toString();
					Double oldValue = Double.parseDouble(oldValue_.substring(oldValue_.indexOf("t") + 1));
					nodeMap.put("status", "current" + oldValue + "/" + probsMarkov.get(k));
					nodesList.add(nodeMap);
				} else if (nodeMap.get("status").toString() == "markov" && (node.equals(selectedNodes.get(k)))) {
					nodeMap.put("status", "current" + probsMarkov.get(k));
					nodesList.add(nodeMap);
				}else if (nodeMap.get("status").toString() == "previous") {
					if (node.equals(selectedNodes.get(k))) {
						nodeMap.put("status", "current" + probsMarkov.get(k));
						nodesList.add(nodeMap);
					} else if (nextNodes.get(k).contains(node)
							&& (nodeMap.get("status") == null || nodeMap.get("status") == "previous")) {
						nodeMap.put("status", "markov");
						nodesList.add(nodeMap);
					}
				}
			}

			for (HashMap nodeItem : nodesList) {
				if (nodeItem.containsValue(node) && !nodeItem.get("status").equals("none")) {
					flag = true;
				}
			}

			if (!flag) {
				nodeMap.put("status", "none");
				nodesList.add(nodeMap);
			}

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

		}

		for (int k = 0; k < phaseHistories.size(); k++) {
			pathString = "";
			for (int l = 0; l < phaseHistories.get(k).size(); l++) {
				pathString += phaseHistories.get(k).get(l) + "-";
			}

			for (int m = 0; m < nextNodes.get(k).size(); m++) {
				pathString += nextNodes.get(k).get(m) + "*-";
			}

			historyList.add(pathString);
		}

		ArrayList<HashMap> _nodesList = new ArrayList<>(nodesList.stream().distinct().collect(Collectors.toList())); // Eliminar
																														// duplicados

		jsonMap.put("nodes", _nodesList);
		jsonMap.put("edges", edgesList);
		ArrayList<String> _historyList = new ArrayList<>(historyList);
		jsonMap.put("routes", _historyList);
		jsonMap.put("done", doneList);
		jsonMap.put("attack", attacks);
		jsonMap.put("ids", ids);
		return gson.toJson(jsonMap);
	}
}
