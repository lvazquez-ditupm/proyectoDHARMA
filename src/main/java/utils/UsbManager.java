package utils;

import com.google.gson.Gson;
import communications.SensorCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UsbManager {

    private HashMap<String, HashMap<String, Object>> output = new HashMap<>();

    public UsbManager(String path) {

        try {
            String input = new String(Files.readAllBytes(Paths.get(path)));

            ArrayList jsonList = new Gson().fromJson(input, ArrayList.class);
            HashMap<String, Object> value = new HashMap<>();
            
            
            jsonList.remove(0);

            value.put("Anomalies", jsonList);
            value.put("Date", System.currentTimeMillis());

            output.put("USB", value);

            SensorCollector.receiveNewData(output);

        } catch (IOException ex) {
            Logger.getLogger(UsbManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
