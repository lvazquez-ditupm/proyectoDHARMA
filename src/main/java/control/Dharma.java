package control;

import core.BAG;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
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

    private static ArrayList<BAG> bagList = new ArrayList<>();
    private static final DharmaProperties props = new DharmaProperties();

    public Dharma() {
        
    }

    /**
     * Crea una nueva red bayesiana a partir del JSON almacenado en la ruta
     * determinada en la configuración del sistema
     */
    public void startNewBAG() {
        BAG bag = new BAG();
        bagList.add(bag);
        try {
            bag.importJSON(props.getJSONPathValue());
        } catch (Exception ex) {
            Logger.getLogger(BAG.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Crea una red bayesiana oculta que no es mostrada al usuario a partir de
     * una normal
     */
    public void setNewPhantomBAG(BAG link) {
        BAG hiddenBag = BAG.clone(bagList.get(bagList.indexOf(link)));
        hiddenBag.setPhantom(link);
        bagList.add(hiddenBag);
    }

    /**
     * Procesa un evento recibido, actualizando el BAG o creando uno nuevo
     *
     * @param eventMap evento recibido
     * @param markovNodes
     * @param markov
     * @param markovID
     * @param probMarkov
     * @param done
     * @param attack
     */
    public void processEvent(HashMap<String, Object> eventMap, ArrayList<String> nodes, boolean markov, int markovID, double probMarkov, double done, HashMap<String, Object> infoAtt, String attack) {

        ArrayList<BAG> bagChangeList = getChangeList((String) eventMap.get("node"));

        if (markov) {
            try {
                if (bagList.isEmpty()) {
                    startNewBAG();
                    BAG bag = bagList.get(bagList.size() - 1);
                    bag.setMarkovID(markovID);
                    bag.setPosition((String) eventMap.get("node"), markovID, bagList, markov, nodes, probMarkov, done, infoAtt, attack);
                    //doActions(bag, (String) eventMap.get("node"));

                } else {
                    boolean flag = false;
                    for (BAG bag : bagList) {
                        if (bag.getMarkovID() == markovID) {
                            bag.setPosition((String) eventMap.get("node"), markovID, bagList, markov, nodes, probMarkov, done, infoAtt, attack);
                            //doActions(bag, (String) eventMap.get("node"));
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        startNewBAG();
                        BAG bag = bagList.get(bagList.size() - 1);
                        bag.setMarkovID(markovID);
                        bag.setPosition((String) eventMap.get("node"), markovID, bagList, markov, nodes, probMarkov, done, infoAtt, attack);
                        //doActions(bag, (String) eventMap.get("node"));
                    }
                }

            } catch (Exception ex) {
                Logger.getLogger(Dharma.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
        /* if (bagChangeList.size() == 1) {
            try {
                BAG bag = bagChangeList.get(0);
                filterPhantom(bag, false);
                orderList();
                bag.setPosition((String) eventMap.get("node"), bagList.indexOf(bag), bagList, markov, probMarkov, done, attack);
                doActions(bag, (String) eventMap.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }
        } else if (bagChangeList.size() > 1) {
            try {
                BAG bag = getWhoContinues(bagChangeList, (String) eventMap.get("node"), true);
                orderList();
                bag.setPosition((String) eventMap.get("node"), bagList.indexOf(bag), bagList, markov, probMarkov, done, attack);
                doActions(bag, (String) eventMap.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }

        } else {
            try {
                if (bagList.isEmpty()) {
                    startNewBAG();
                }
                BAG bag = bagList.get(bagList.size() - 1);
                bag.setPosition((String) eventMap.get("node"), bagList.size() - 1, bagList, markov, probMarkov, done, attack);
                doActions(bag, (String) eventMap.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }

        }
        deleteFolder(-1);*/
    }

    /**
     * Si varios grafos continuan al mismo nodo, estima cual es el que avanza
     *
     * @param bagChangeCandidates candidatos a continuar
     * @param eventString evento al que hay que avanzar
     * @param filter indica si hay grafos fantasma que deben ser eliminados
     * @return grafo actualizado
     */
    public BAG getWhoContinues(ArrayList<BAG> bagChangeCandidates, String eventString, boolean filter) {

        BAG nextBAG;
        nextBAG = correlateNextBag(bagChangeCandidates);
        BAG phantomPreviousBag;

        if (nextBAG == null) {

            double maxWeight = -1;

            for (BAG bag : bagChangeCandidates) {
                if (bag.getWeight(bag.getLastNode(), eventString) > maxWeight) {
                    maxWeight = bag.getWeight(bag.getLastNode(), eventString);
                    nextBAG = bag;
                } else if (bag.getWeight(bag.getLastNode(), eventString) == maxWeight
                        && bag.getTime() < nextBAG.getTime() && bag.getCurrentTime().compareTo(Calendar.getInstance()) >= -10000) {  //To Do cambiar la fecha mínima para considerar 
                    nextBAG = bag;
                }
            }

            if (filter) {
                if (nextBAG.isPhantom()) {
                    repairBAG(nextBAG, eventString);
                } else {
                    filterPhantom(nextBAG, true);
                }

            }
        }

        return nextBAG;
    }
/*
    /**
     * Repara el grafo si ante una coincidencia se ha hecho avanzar uno por
     * probabilidad y posteriormente se comprueba que es otro el grafo que
     * debería haber avanzado
     *
     * @param bag grafo a modificar
     * @param eventString evento ocurrido
     */
    public void repairBAG(BAG bag, String eventString) {

        BAG original = bag.getLink();
        String mutualNode = bag.getLastNode();
        String nextNode = original.getLastNode();

        bagList.remove(original);
        bag.setReal();

        ArrayList<BAG> candidates = getChangeList(nextNode);
        candidates.remove(bag);

        BAG repairedBAG = getWhoContinues(candidates, nextNode, false);

        //bag.setPosition(eventString, bagList.indexOf(bag), bagList);
        if (repairedBAG != null) {
            try {
                repairedBAG.setPosition(nextNode, bagList.indexOf(repairedBAG), bagList, false, repairedBAG.getMarkovNodes(), repairedBAG.getProbMarkov(), repairedBAG.getDone(), repairedBAG.getinfoAtt(), repairedBAG.getAttack());
            } catch (Exception ex) {
                Logger.getLogger(Dharma.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Elimina grafos ocultos que no van a ser necesitados
     *
     * @param collision indica que hay colisión entre dos BAG, para crear uno
     * nuevo fantasma
     * @param nextBAG
     */
    public void filterPhantom(BAG nextBAG, boolean collision) {

        boolean newPhantomFlag = true;

        for (BAG bag : bagList) {
            if (bag.isPhantom() && bag.getLink() == nextBAG) {
                bagList.remove(bag);
                newPhantomFlag = false;
                break;
            }
        }

        if (newPhantomFlag && nextBAG.numberOfEdges(nextBAG.getLastNode()) > 1 && collision) {
            setNewPhantomBAG(nextBAG);
        }
    }

    /**
     * Ordena la lista de BAGs para dejar los ocultos al final (y evitar huecos
     * en la visualización de grafos
     */
    public void orderList() {
        ArrayList<BAG> orderedList = new ArrayList<>();

        for (BAG item : bagList) {
            if (!item.isPhantom()) {
                orderedList.add(item);
            }
        }
        for (BAG item : bagList) {
            if (item.isPhantom()) {
                orderedList.add(item);
            }
        }
        bagList = orderedList;
    }

    /**
     * Elimina los ficheros JSON de grafos eliminados
     */
    public static void deleteFolder() {
        File folder = new File(props.getBagVisualizatorPathValue() + "/public");
        File[] files = folder.listFiles();
        ArrayList<BAG> visibleBagList = new ArrayList<>();
        ArrayList<Integer> existingGraphs = new ArrayList<>();
        String nombre;
        int id;

        existingGraphs.add(0);
        for (BAG bag : bagList) {
            if (!bag.isPhantom()) {
            	existingGraphs.add(bag.getMarkovID());
                visibleBagList.add(bag);
            } else {
                break;
            }
        }

        if (files != null) {
            for (File f : files) {
                nombre = f.getName();
                id = Integer.parseInt(nombre.substring(nombre.indexOf("datos") + 5, nombre.indexOf(".json")));
                if (!existingGraphs.contains(id)) {
                    f.delete();
                }
            }
        }

    }

    /**
     * Devuelve una lista con los BAGs que pueden avanzar al nuevo nodo
     *
     * @param node nodo del evento recibido
     * @return posibles avances al nodo
     */
    private ArrayList<BAG> getChangeList(String node) {
        ArrayList<BAG> bagChangeList = new ArrayList<>();
        for (BAG bag : bagList) {
            if (bag.connected(bag.getLastNode(), node)
                    && bag.getNodeStatus(node).equals("none")) {
                bagChangeList.add(bag);
            }
        }
        return bagChangeList;
    }

    /**
     * Elimina los nodos previstos por HMM del BAG que los posea
     *
     * @param markovNodes cadena de nodos a borrar
     */
    public void removeMarkov(ArrayList<String> markovNodes) {
        for (BAG bag : bagList) {
            bag.removeMarkov(markovNodes);
        }
    }

    /**
     * Elimina el BAG seleccionado
     *
     * @param id identificador del grafo
     */
    public void removeBAG(int id) {
        try {
            for (BAG bag : bagList) {
            	//System.out.println(bag.getMarkovID());
            	//System.out.println(id);
                if (bag.getMarkovID() == id) {
                    bagList.remove(bag);
                    break;
                }
            }
            for (BAG bag : bagList) {
                bag.exportIndividualJSON(bag.getMarkovID());
            }

            if (bagList.isEmpty()) {
                BAG.exportCleanJSON();
            } else {
                bagList.get(0).exportCompleteJSON(bagList);
            }
            deleteFolder();

        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(Dharma.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

   
    /**
     * Correla los datos de los eventos pasados para inferir el BAG que avanza
     *
     * @param bagChangeCandidates candidatos a avanzar
     * @return BAG a avanzar
     */
    public BAG correlateNextBag(ArrayList<BAG> bagChangeCandidates) {

        //To Do
        System.err.println("To Do!");
        return null;
    }
}
