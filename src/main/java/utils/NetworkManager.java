package utils;

import com.google.gson.Gson;
import communications.SensorCollector;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class handles the HMM data in order to be used as a network sensor
 *
 */
public class NetworkManager {

    public static void processData(String node, ArrayList<String> nodes, int markovID,
            double probMarkov, double done, HashMap<String, Object> infoAtt, String attack) {

        HashMap output = new HashMap<>();
        HashMap HMM = new HashMap<>();
        HashMap data = new HashMap<>();
        Gson gson = new Gson();
        
        HMM.put("node", node);
        HMM.put("nodes", nodes);
        HMM.put("ID", markovID);
        HMM.put("prob", probMarkov);
        HMM.put("done", done);
        HMM.put("info", infoAtt);
        HMM.put("name", attack);
        data.put("Date", System.currentTimeMillis());
        data.put("HMM", gson.toJson(HMM));
        output.put("HMM", data);

        SensorCollector.receiveNewData(output);
    }
}
