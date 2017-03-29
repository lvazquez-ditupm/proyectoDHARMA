package utils;

import com.google.gson.Gson;
import communications.SensorCollector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
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
            //Logger.getLogger(PaeManager.class.getName()).log(Level.SEVERE, null, ex);
            System.err.println("Error al acceder a los datos del sensor PAE");
            new PaeManager(path);
        }
    }

}
