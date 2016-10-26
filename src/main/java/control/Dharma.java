package control;

import core.ActionController;
import core.BAG;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.PropsUtil;

/**
 * This class represents the DHARMA main
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Dharma {

    private ArrayList<BAG> bagList = new ArrayList<>();
    private final PropsUtil props = new PropsUtil();

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
     * @param eventString evento recibido
     */
    public void processEvent(HashMap<String, Object> eventString) {

        ArrayList<BAG> bagChangeList = getChangeList((String) eventString.get("node"));

        if (bagChangeList.size() == 1) {
            try {
                BAG bag = bagChangeList.get(0);
                filterPhantom(bag, false);
                orderList();
                bag.setPosition((String) eventString.get("node"), bagList.indexOf(bag), bagList);
                doActions(bag, (String) eventString.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }
        } else if (bagChangeList.size() > 1) {
            try {
                BAG bag = getWhoContinues(bagChangeList, (String) eventString.get("node"), true);
                orderList();
                bag.setPosition((String) eventString.get("node"), bagList.indexOf(bag), bagList);
                doActions(bag, (String) eventString.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }

        } else {
            startNewBAG();
            try {
                BAG bag = bagList.get(bagList.size() - 1);
                bag.setPosition((String) eventString.get("node"), bagList.size() - 1, bagList);
                doActions(bag, (String) eventString.get("node"));
            } catch (Exception ex) {
                System.err.println("Nodo no existente en la red");
            }

        }

        deleteFolder();
    }

    /**
     * Si varios grafos continuan al mismo nodo, estima cual es el que avanza
     *
     * @param bagChangeCandidates candidatos a continuar
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
                        && bag.getTime() < nextBAG.getTime() && bag.getCurrentTime().compareTo(Calendar.getInstance()) >= -10000) {  //cambiar
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
        undoActions(original, nextNode);
        bag.setReal();

        ArrayList<BAG> candidates = getChangeList(nextNode);
        candidates.remove(bag);

        BAG repairedBAG = getWhoContinues(candidates, nextNode, false);

        //bag.setPosition(eventString, bagList.indexOf(bag), bagList);
        if (repairedBAG != null) {
            try {
                repairedBAG.setPosition(nextNode, bagList.indexOf(repairedBAG), bagList);
                doActions(repairedBAG, nextNode);
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
     * @param bagChangeCandidates
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
    private void deleteFolder() {
        File folder = new File(props.getBagVisualizatorPathValue()+"/public");
        File[] files = folder.listFiles();
        ArrayList<BAG> visibleBagList = new ArrayList<>();
        String nombre;
        int id;

        for (BAG bag : bagList) {
            if (!bag.isPhantom()) {
                visibleBagList.add(bag);
            } else {
                break;
            }
        }

        if (files != null) {
            for (File f : files) {
                nombre = f.getName();
                id = Integer.parseInt(nombre.substring(nombre.indexOf("datos") + 5, nombre.indexOf(".json")));
                if (id > visibleBagList.size()) {
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
     * Ejecuta las acciones relacionadas con el nodo
     *
     * @param bag
     * @param node
     */
    public void doActions(BAG bag, String node) {
        ActionController.doActions(bag, node);
    }

    /**
     * Ante una corrección del grafo, deshace las acciones del nodo y BAG
     * seleccionados
     *
     * @param bag
     * @param node
     */
    public void undoActions(BAG bag, String node) {
        ActionController.undoActions(bag, node);
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
