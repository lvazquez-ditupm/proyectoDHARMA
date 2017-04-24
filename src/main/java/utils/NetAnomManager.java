package utils;

import communications.SensorCollector;
import java.util.HashMap;

/**
 * This class handles the network anomaly value
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class NetAnomManager {

    public NetAnomManager(String anomValue) {
        HashMap output = new HashMap<>();
        HashMap netAnom = new HashMap<>();
        HashMap data = new HashMap<>();

        netAnom.put("Anomaly", Long.parseLong(anomValue));
        data.put("Date", System.currentTimeMillis());
        output.put("NetAnomaly", data);

        SensorCollector.receiveNewData(output);
    }
    
}
