package communications;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author root
 */
public class SensorCollector {

    static ArrayList<String> paths;

    public SensorCollector(String inputsFile) {

        FileReader fileReader = null;
        try {
            fileReader = new FileReader(inputsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            paths = new ArrayList<String>(Arrays.asList(lines.toArray(new String[lines.size()])));
        } catch (Exception ex) {
            Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        processData();

    }

    private void processData() {
        for (String path : paths){
            String item = path.split("///")[0];
            String path_ = path.split("///")[1];
            switch(item){
                case "A":
            }
        }
    }
    
    
    

}
