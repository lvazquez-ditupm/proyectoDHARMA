package control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import utils.DharmaProperties;

/**
 * This class executes all the external modules
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SensorConfigurator implements Runnable {

    private static final DharmaProperties props = new DharmaProperties();

    @Override
    public void run() {

        System.out.println("****  Iniciando sensores  ****");
        try {
            execPython();
        } catch (ScriptException | IOException ex) {
            Logger.getLogger(SensorConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Ejecuta un script en Python para arrancar todos los elementos externos
     * necesarios para el funcionamiento del sistema
     */
    public static void execPython() throws ScriptException, IOException {

        String s = null;

        try {

            Process p = Runtime.getRuntime().exec("sudo python " + props.getDharmaPathValue() + "/loadScript.py " + props.getAnomalyPathsValue());
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
