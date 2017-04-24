package utils;

import communications.SensorCollector;
import java.util.HashMap;

/**
 * This class handles IDS alerts
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class IDSManager {

    public IDSManager(String alert) {
        HashMap output = new HashMap<>();
        HashMap IDS = new HashMap<>();
        HashMap data = new HashMap<>();

        IDS.put("Anomaly", alert);
        data.put("Date", System.currentTimeMillis());
        output.put("IDS", data);

        SensorCollector.receiveNewData(output);
    }
    
}
