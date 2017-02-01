package communications;

import control.Dharma;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.DharmaProperties;
import utils.SECEventParser;

/**
 * This class represents the detector of new SEC events written in a file
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SECEventReceiver implements Runnable {

    private final DharmaProperties props = new DharmaProperties();
    Dharma dharma = new Dharma();

    public SECEventReceiver() {
    }

    @Override
    public void run() {

        BufferedReader idmefDatos = null;
        String xml_file = props.getEventLogPathValue();
        try {
            String str;
            idmefDatos = new BufferedReader(new FileReader(xml_file));
            String str2;
            while (true) {
                try {
                    while (idmefDatos.ready()) {
                        str = idmefDatos.readLine();
                        if (!str.equals("")) {
                            // HashMap<String, Object> nodeData =
                            // SECEventParser.parse(str);
                            HashMap<String, Object> nodeData = new HashMap<>();
                            nodeData.put("node", str);
                            if (!nodeData.isEmpty()) {
                                //TODO
                            }

                        }
                    }
                } catch (IOException ex) {
                    Logger.getLogger(SECEventReceiver.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
