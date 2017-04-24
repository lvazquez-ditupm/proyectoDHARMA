package control;

import correlator.DataCataloger;
import core.Graph;
import communications.LogReceiver;
import communications.SECEventReceiver;
import communications.SensorCollector;
import correlator.ActivePeriods;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.CorrelatorManager;
import utils.DharmaProperties;

/**
 * This class starts the DHARMA system
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Main {

    private static SECEventReceiver eventReceiver;
    private static SensorConfigurator sensorConfigurator;
    private static DataCataloger dataCataloger;
    private static LogReceiver logReceiver;
    private static Dharma dharma;
    private static MarkovController markovController;
    private static SensorCollector sensorCollector;
    private static final DharmaProperties props = new DharmaProperties();

    public static void main(String[] args) {

        try {
            Graph.exportCleanJSON();
            //markovController = new MarkovController();
            //markovController.parse("IDAtaque=1;TipoAtaque=Denegacion de Servicio;Nodos=Intento de intrusion,Buffer Overflow,Denegacion de Servicio;Estado=Buffer Overflow;PEstado=0.9998914037855784;PFinal=0.6231988799803523;Risk=2;Markovid=1");
            Dharma dharma = new Dharma();
            //dharma.removeGraph(1);

            new CorrelatorManager(props.getDatasetPathValue());
            eventReceiver = new SECEventReceiver();
            new Thread(eventReceiver).start();
            ActivePeriods.create();
            
            dataCataloger = new DataCataloger();
            new Thread(dataCataloger).start();
            
            sensorConfigurator = new SensorConfigurator();
            new Thread(sensorConfigurator).start();

            sensorCollector = new SensorCollector(props.getAnomalyPathsValue());
            new Thread(sensorCollector).start();          
            
            logReceiver = new LogReceiver(Integer.parseInt(args[1]), args[0]);
            logReceiver.start();

        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
