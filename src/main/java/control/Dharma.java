package control;

import core.BAG;
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
	 * Procesa un evento recibido, actualizando el BAG o creando uno nuevo
	 *
	 * @param eventMap
	 *            evento recibido
	 * @param markovNodes
	 * @param markov
	 * @param markovID
	 * @param probMarkov
	 * @param done
	 * @param attack
	 */
	public void processEvent(HashMap<String, Object> eventMap, ArrayList<String> nodes, int markovID,
			double probMarkov, double done, HashMap<String, Object> infoAtt, String attack) {

		try {
			if (bagList.isEmpty()) {
				startNewBAG();
				BAG bag = bagList.get(bagList.size() - 1);
				bag.setMarkovID(markovID);
				bag.setPosition((String) eventMap.get("node"), markovID, bagList, nodes, probMarkov, done,
						infoAtt, attack);

			} else {
				boolean flag = false;
				for (BAG bag : bagList) {
					if (bag.getMarkovID() == markovID) {
						bag.setPosition((String) eventMap.get("node"), markovID, bagList, nodes, probMarkov,
								done, infoAtt, attack);
						flag = true;
						break;
					}
				}
				if (!flag) {
					startNewBAG();
					BAG bag = bagList.get(bagList.size() - 1);
					bag.setMarkovID(markovID);
					bag.setPosition((String) eventMap.get("node"), markovID, bagList, nodes, probMarkov, done,
							infoAtt, attack);
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
		File folder = new File(props.getBagVisualizatorPathValue() + "/public");
		File[] files = folder.listFiles();
		ArrayList<BAG> visibleBagList = new ArrayList<>();
		ArrayList<Integer> existingGraphs = new ArrayList<>();
		String nombre;
		int id;

		existingGraphs.add(0);
		for (BAG bag : bagList) {
			
				existingGraphs.add(bag.getMarkovID());
				visibleBagList.add(bag);
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
	 * Elimina el BAG seleccionado
	 *
	 * @param id
	 *            identificador del grafo
	 */
	public void removeBAG(int id) {
		try {
			for (BAG bag : bagList) {
				// System.out.println(bag.getMarkovID());
				// System.out.println(id);
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
			Logger.getLogger(Dharma.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
