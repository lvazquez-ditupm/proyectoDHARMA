package control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import utils.DharmaProperties;

/**
 * This class represents the controller which select the actions related to a
 * certain node of a certain bayesian graph
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SensorConfigurator implements Runnable {

    private static final DharmaProperties props = new DharmaProperties();

    @Override
    public void run() {

        System.out.println("****  Iniciando sensores  ****");
        stopAll();
        try {
            execPython();
        } catch (ScriptException | IOException ex) {
            Logger.getLogger(SensorConfigurator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private static void stopAll() {
        //TODO
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
