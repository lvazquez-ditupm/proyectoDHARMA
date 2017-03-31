package core;

import java.util.HashMap;
import smile.Network;

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

    public BayesNetworkManager(int id) {
        net = new Network();
        net.readFile("bayesNet" + id + ".xdsl");
        nodes = net.getAllNodeIds();
    }

    /**
     * Actualiza las tablas de probabilidad condicional de aquellos nodos que no
     * hayan tenido una evidencia
     *
     * @param id identificador de grafo
     */
    public void updateProbs(int id) {
        for (String node : nodes) {
            if (!net.isEvidence(node)) {
                if (net.getParentIds(node).length == 0) {
                    double[] rootProb = {0.5, 0.5};
                    net.setNodeDefinition(node, rootProb);
                } else {
                    double[] childProb = calcProbs(node, net.getParentIds(node));
                    net.setNodeDefinition(node, childProb);
                }
            }
        }
        net.writeFile("bayesNet" + id + ".xdsl");
    }

    /**
     * Establece una evidencia positiva en un nodo
     *
     * @param node nodo
     */
    public void eventOcurred(String node) {
        net.setEvidence(adjustName(node), "True");
        net.updateBeliefs();
        filterNodes();
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
            res.put(node, net.getNodeValue(adjustName(node))[1]);
        }
        return res;
    }

    /**
     * Calcula las tablas de probabilidad condicional de un nodo
     *
     * @param node nodo
     * @param parentIds nodos padres
     * @return CPT
     */
    private double[] calcProbs(String node, String[] parentIds) {
        double[] probTrue = new double[(int) Math.pow(2, parentIds.length)];
        double[] probFalse = new double[(int) Math.pow(2, parentIds.length)];
        double[] cpt = new double[(int) Math.pow(2, parentIds.length + 1)];

        for (int i = 0; i < probTrue.length; i++) {
            probTrue[i] = Math.random();
        }

        for (int i = 0; i < probTrue.length; i++) {
            probFalse[i] = 1 - probTrue[i];
        }

        int j = 0;
        for (int i = 0; i < cpt.length; i += 2) {
            cpt[i] = probTrue[j];
            cpt[i + 1] = probFalse[j];
            j++;
        }
        return cpt;
    }

    /**
     * Cuando es imposible llegar a un nodo desde el "current", se pone su
     * evidencia a 0
     */
    private void filterNodes() {
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
    }

    /**
     * Elimina los espacios cambiándolos por barras bajas
     */
    private String adjustName(String name) {
        if (name.contains(" ")) {
            name = name.replace(" ", "_");
        }
        return name;
    }
}
