package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import communications.SensorCollector;
import java.util.HashMap;

/**
 * This class handles the correlator data. It get the values, adds a date and a
 * tag to identify the correlation
 *
 */
public class CorrelatorManager {

    private final File f;
    private List<File> files;
    private Gson gson = new Gson();

    HashMap<String, Integer> anomaliesPerFile = new HashMap<>();
    HashMap<String, HashMap> anomaliesTotal = new HashMap<>();
    HashMap<String, Object> value = new HashMap<>();
    HashMap<String, HashMap<String, Object>> output = new HashMap<>();

    public CorrelatorManager(String path) {
        f = new File(path);

        getFiles(f);
        for (File file : files) {
            if (file.getName().contains("anomalies")) {
                try {
                    anomaliesTotal.put(file.getName(), getAnomalies(new String(Files.readAllBytes(file.toPath()), "UTF-8")));
                } catch (IOException ex) {
                    Logger.getLogger(CorrelatorManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        value.put("Date", System.currentTimeMillis());
        value.put("Anomalies", anomaliesTotal);
        output.put("Correlator-" + f.getName(), value);

        SensorCollector.receiveNewData(output);

    }

    /**
     * Devuelve una lista con todos los ficheros existentes en un directorio
     *
     * @param root directorio raiz
     */
    private void getFiles(File root) {

        File[] files_ = root.listFiles();
        files = new ArrayList<>();

        for (File file : files_) {
            if (file.isFile()) {
                files.add(file);
            }
        }
    }

    private HashMap<String, Integer> getAnomalies(String string) {

        String json = (String) gson.fromJson(string, HashMap.class).get("anomalies");
        String[] jsonSplitted = json.split(",");
        String[] aux;
        int anValue;

        for (int i = 0; i < jsonSplitted.length; i++) {

            aux = jsonSplitted[i].split(":");
            anValue = Integer.parseInt(aux[1]);
            anomaliesPerFile.put(aux[0], anValue);
        }

        return anomaliesPerFile;
    }
}
