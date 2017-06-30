package utils;

import com.google.gson.Gson;
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
        HashMap data = new HashMap<>();
        HashMap anomaly = new Gson().fromJson(alert, HashMap.class);

        data.put("Anomaly", anomaly);
        data.put("Date", System.currentTimeMillis());
        output.put("IDS", data);

        SensorCollector.receiveNewData(output);
     }
    
}
