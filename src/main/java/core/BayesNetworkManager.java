package core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import smile.Network;
import smile.learning.DataMatch;
import smile.learning.DataSet;
import smile.learning.EM;

/**
 * This class manages the Bayes Net, in order to know the probabilities of
 * reaching a node
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class BayesNetworkManager {

    private Network net;
    private String[] nodes;

    public BayesNetworkManager() {
        net = new Network();
        net.readFile("bayesNet.xdsl");
        updateProbs();
        nodes = net.getAllNodeIds();
    }

    /**
     * Actualiza las tablas de probabilidad condicional
     *
     */
    public void updateProbs() {
        DataSet ds = new DataSet();
        ds.readFile("bayesNet.txt");
        ds.matchNetwork(net);
        DataMatch[] matching = ds.matchNetwork(net);
        final EM em = new EM();
        //em.setUniformizeParameters(true);
        //em.setRandomizeParameters(false);
        //em.setEqSampleSize(5);
        em.learn(ds, net, matching);
        net.updateBeliefs();
        net.writeFile("bayesNet.xdsl");
    }

    /**
     * Establece una evidencia positiva en un nodo
     *
     * @param node nodo
     */
    public void eventOcurred(String node) {
        net.setEvidence(spacesToUnderscore(node), "True");
        net.updateBeliefs();
        //filterNodes();
    }

    /**
     * Obtiene las probabilidades de ocurrencia de todos los nodos
     *
     * @return mapa de nodos con sus probabilidades de ocurrencia
     */
    public HashMap<String, Double> getEventProbs() {
        HashMap<String, Double> res = new HashMap<>();
        net.updateBeliefs();
        for (String node : nodes) {
            res.put(node, net.getNodeValue(spacesToUnderscore(node))[1]);
        }
        return res;
    }

    /**
     * Cuando es imposible llegar a un nodo desde el "current", se pone su
     * evidencia a 0
     */
    /*private void filterNodes() {
        //Los nodos hijos de los "previous" se evidencian a 0
        for (String node : nodes) {
            if (net.isEvidence(node)) {
                for (String child : net.getChildIds(node)) {
                    if (net.isEvidence(child) && net.getNodeValue(child)[1] == 1.0
                            && net.getNodeValue(node)[1] == 1.0) {
                        for (String child_ : net.getChildIds(node)) {
                            if (!net.isEvidence(child_)) {
                                net.setEvidence(child_, "False");
                            }
                        }
                        net.updateBeliefs();
                        break;
                    }
                }
            }
        }

        //Los nodos evidenciados a 0 que pueden alcanzarse desde current, se eliminan su evidencia
        boolean flag = false;
        for (String node : nodes) {
            if (net.isEvidence(node) && net.getNodeValue(node)[1] == 1.0) {
                for (String child : net.getChildIds(node)) {
                    if (net.isEvidence(child)) {
                        flag = true;
                    }
                    if (!flag) {
                        for (String child_ : net.getChildIds(node)) {
                            if (net.isEvidence(child_)) {
                                net.clearEvidence(child_);
                                net.updateBeliefs();
                            }
                        }
                    }
                    flag = false;
                }
            }
        }

        //Si a un nodo sólamente se puede acceder a través de otro con evidencia 0, se pone a 0 también
        boolean retry;
        do {
            retry = false;
            flag = false;

            for (String node : nodes) {
                if (!net.isEvidence(node)) {
                    for (String parent : net.getParentIds(node)) {
                        if (!net.isEvidence(parent) || net.getNodeValue(parent)[1] == 1.0) {
                            flag = true;
                        }
                    }
                    if (!flag) {
                        net.setEvidence(node, "False");
                        net.updateBeliefs();
                        retry = true;
                    }
                    flag = false;
                }
            }
        } while (retry);
    }*/

    /**
     * Modifica las CPT de ciertos nodos
     *
     * @param variation variacion para cada nodo
     */
    public void timeWindow(String reachedNode, double variation) {
        reachedNode = spacesToUnderscore(reachedNode);
        for (String node : nodes) {
            String[] parents = net.getParentIds(node);
            if (!Arrays.asList(parents).contains(reachedNode)) {
                continue;
            }
            double[] cpt = net.getNodeDefinition(node);

            int i;
            for (i = 0; i < parents.length; i++) {
                if (parents[i].equals(reachedNode)) {
                    break;
                }
            }

            int factor = (int) Math.pow(2, (parents.length - i));
            boolean flag1 = false;
            boolean flag2 = false;
            int j = 0;

            for (i = 0; i < cpt.length; i++) {
                if (flag1) {
                    if (flag2) {
                        cpt[i] += variation;
                    } else {
                        cpt[i] -= variation;
                    }

                    flag2 = !flag2;
                }
                j++;
                if (j == factor) {
                    j = 0;
                    flag1 = !flag1;
                }
            }
            net.setNodeDefinition(node, cpt);
            net.writeFile("bayesNet2.xdsl");
        }
    }

    /**
     * Elimina los espacios cambiándolos por barras bajas
     */
    private String spacesToUnderscore(String name) {
        if (name.contains(" ")) {
            name = name.replace(" ", "_");
        }
        return name;
    }

    /**
     * Cambia las barras bajas por espacios
     */
    private static String underscoreToSpace(String name) {
        if (name.contains("_")) {
            name = name.replace("_", " ");
        }
        return name;
    }

    /**
     * Actualiza el dataset de aprendizaje de la red
     *
     * @param nodes nodos transcurridos hasta la finalización del ataque
     */
    public static void updateHistory(ArrayList<String> nodes) {
        try {
            Path myPath = Paths.get("./bayesNet.txt");
            String[] strArray = Files.lines(myPath).map(s -> s.split(",")).findFirst().get();
            String newLine = "";
            for (String item : strArray) {
                if (nodes.contains(underscoreToSpace(item))) {
                    newLine += "True,";
                } else {
                    newLine += "False,";
                }
            }
            newLine = newLine.substring(0, newLine.length() - 1);
            newLine += "\n";
            Writer output = new BufferedWriter(new FileWriter("./bayesNet.txt", true));
            output.append(newLine);
            output.close();
            new BayesNetworkManager().updateProbs();

        } catch (IOException ex) {
            Logger.getLogger(BayesNetworkManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
