package control;

import communications.LogReceiver;
import core.BAG;
import core.BayesNetworkManager;
import java.util.HashMap;


/**
 * This class starts the system
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Main {

    private static LogReceiver logReceiver;

    public static void main(String[] args) {

        BAG.exportCleanJSON();
        logReceiver = new LogReceiver(Integer.parseInt(args[1]), args[0]);
        logReceiver.start();  
    }
}
