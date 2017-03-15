package control;

import core.Graph;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.DharmaProperties;

/**
 * This class represents the DHARMA main
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Dharma {

    private static ArrayList<Graph> graphList = new ArrayList<>();
    private static final DharmaProperties props = new DharmaProperties();

    public Dharma() {

    }

    /**
     * Crea una nueva red bayesiana a partir del JSON almacenado en la ruta
     * determinada en la configuración del sistema
     * @param markovID identificador del HMM
     */
    public void startNewGraph(int markovID) {
        Graph graph = new Graph(markovID);
        graphList.add(graph);
    }

    /**
     * Procesa un evento recibido, actualizando el grafo o creando uno nuevo
     *
     * @param eventMap evento recibido
     * @param nodes nodos involucrados en el ataque
     * @param markovID ID de la cadena que lleva el evento
     * @param infoAtt otros datos sobre el ataque
     * @param probMarkov probabilidad de estar en el estado que dice la cadena
     * @param done porcentaje del ataque realizado
     * @param attack nombre del ataque
     */
    public void processEvent(HashMap<String, Object> eventMap, ArrayList<String> nodes, int markovID,
            double probMarkov, double done, HashMap<String, Object> infoAtt, String attack) {

        try {
            if (graphList.isEmpty()) {
                startNewGraph(markovID);
                Graph graph = graphList.get(graphList.size() - 1);
                graph.setPosition((String) eventMap.get("node"), markovID,
                        graphList, nodes, probMarkov, done, infoAtt, attack);

            } else {
                boolean flag = false;
                for (Graph graph : graphList) {
                    if (graph.getMarkovID() == markovID) {
                        graph.setPosition((String) eventMap.get("node"), markovID,
                                graphList, nodes, probMarkov, done, infoAtt, attack);
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    startNewGraph(markovID);
                    Graph graph = graphList.get(graphList.size() - 1);
                    graph.setPosition((String) eventMap.get("node"), markovID,
                            graphList, nodes, probMarkov, done, infoAtt, attack);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(Dharma.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Elimina los ficheros JSON de grafos eliminados
     */
    public static void deleteFolder() {
        File folder = new File(props.getGraphVisualizatorPathValue() + "/public");
        File[] files = folder.listFiles();
        ArrayList<Graph> visibleGraphList = new ArrayList<>();
        ArrayList<Integer> existingGraphs = new ArrayList<>();
        String nombre;
        int id;

        existingGraphs.add(0);
        for (Graph graph : graphList) {

            existingGraphs.add(graph.getMarkovID());
            visibleGraphList.add(graph);
        }

        if (files != null) {
            for (File f : files) {
                nombre = f.getName();
                id = Integer.parseInt(nombre.substring(nombre.indexOf("datos") + 5,
                        nombre.indexOf(".json")));
                if (!existingGraphs.contains(id)) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Elimina el grafo seleccionado
     *
     * @param id identificador de la cadena HMM que generó el grafo
     */
    public void removeGraph(int id) {
        try {
            for (Graph graph : graphList) {
                if (graph.getMarkovID() == id) {
                    graphList.remove(graph);
                    break;
                }
            }
            for (Graph graph : graphList) {
                graph.exportIndividualJSON(graph.getMarkovID());
            }

            if (graphList.isEmpty()) {
                Graph.exportCleanJSON();
            } else {
                graphList.get(0).exportCompleteJSON(graphList);
            }
            deleteFolder();

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Dharma.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
