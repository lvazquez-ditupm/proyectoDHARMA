package core;

import java.util.ArrayList;
import java.util.HashMap;
import smile.Network;

public class BayesNetworkManager {

    private Network net;
    private String[] nodes;

    public BayesNetworkManager(int id) {
        net = new Network();
        net.readFile("bayesNet" + id + ".xdsl");
        nodes = net.getAllNodeIds();
    }

    public void updateProbs(int id) {
        for (String node : nodes) {
            if (net.getParentIds(node).length == 0) {
                double[] rootProb = {0.5, 0.5};
                net.setNodeDefinition(node, rootProb);

            } else {
                double[] childProb = calcProbs(node, net.getParentIds(node));
                net.setNodeDefinition(node, childProb);
            }
        }
        net.writeFile("bayesNet" + id + ".xdsl");
        net.readFile("bayesNet" + id + ".xdsl");
    }

    public void eventOcurred(String node) {
        net.setEvidence(node, "True");
        filterNodes();
    }

    public void clearEvidences() {
        net.clearAllEvidence();
    }

    public HashMap<String, Double> getProbs() {
        net.updateBeliefs();
        HashMap<String, Double> result = new HashMap<>();

        for (String node : nodes) {
            result.put(node, net.getNodeValue(node)[1]);
        }
        return result;
    }

    public HashMap<String, Double> getEventProbs() {
        HashMap<String, Double> res = new HashMap<>();
        net.updateBeliefs();
        for (String node : nodes) {

            res.put(node, net.getNodeValue(node)[1]);

        }
        return res;
    }

    private double[] calcProbs(String node, String[] parentIds) {
        double[] probTrue = new double[(int) Math.pow(2, parentIds.length)];
        double[] probFalse = new double[(int) Math.pow(2, parentIds.length)];
        double[] res = new double[(int) Math.pow(2, parentIds.length + 1)];

        for (int i = 0; i < probTrue.length; i++) {
            probTrue[i] = Math.random();
        }

        for (int i = 0; i < probTrue.length; i++) {
            probFalse[i] = 1 - probTrue[i];
        }

        int j = 0;
        for (int i = 0; i < res.length; i += 2) {
            res[i] = probTrue[j];
            res[i + 1] = probFalse[j];
            j++;
        }
        return res;
    }

    private String[] filterNodes() {
        ArrayList<String> resList = new ArrayList<String>();
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
                        break;
                    }
                }
            }
        }

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
                            }
                        }
                    }
                    flag = false;
                }
            }
        }

        String[] res = new String[resList.size()];
        return resList.toArray(res);
    }
}
