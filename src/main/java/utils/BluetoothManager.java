package utils;

import communications.SensorCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages bluetooth anomalies. It parses them and adds a date
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class BluetoothManager {

    private HashMap<String, HashMap<String, Object>> output = new HashMap<>();
    private HashMap<String, Object> anomalies = new HashMap<>();

    public BluetoothManager(String path) {
        try {
            String input = new String(Files.readAllBytes(Paths.get(path)));
            if (input.equals("\n")) {
                return;
            }
            input = input.replace("\n", "");
            String[] addressArray = input.split(", ");
            List<String> addresses = Arrays.asList(addressArray);
            anomalies.put("Anomalies", addresses);
            anomalies.put("Date", System.currentTimeMillis());
            output.put("Bluetooth", anomalies);
            SensorCollector.receiveNewData(output);
        } catch (IOException ex) {
            Logger.getLogger(BluetoothManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
