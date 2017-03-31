package utils;

import com.google.gson.Gson;
import communications.SensorCollector;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * This class manages PAE anomalies. It parses them and adds a date
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class PaeManager {
    
    private HashMap<String, HashMap<String, Object>> output = new HashMap<>();
    
    public PaeManager(String path) {
        try {
            String input = new String(Files.readAllBytes(Paths.get(path)));
            HashMap value = new Gson().fromJson(input, HashMap.class);
            value.put("Date", System.currentTimeMillis());
            output.put("PAE", value);
            SensorCollector.receiveNewData(output);
        } catch (Exception ex) {
            System.err.println("ERROR: Error al acceder a los datos del sensor PAE");
        }
    }
}
