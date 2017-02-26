package control;

import communications.LogReceiver;
import core.BAG;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.MySQL;

/**
 * This class starts the system
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Main {
    
    private static LogReceiver logReceiver;
    
    public static void main(String[] args) {
        
            MySQL.getRoomStatus(14, 2, 190);
            //BAG.exportCleanJSON();
            //MarkovController mc = new MarkovController();
            //mc.parse("IDAtaque=1;TipoAtaque=Denegacion de Servicio;Nodos=Intento de intrusion,Buffer Overflow,Denegacion de Servicio;Estado=Buffer Overflow;PEstado=0.9998914037855784;PFinal=0.6231988799803523;Risk=2;Markovid=1");
            //logReceiver = new LogReceiver(Integer.parseInt(args[1]), args[0]);
            //logReceiver.start();  

    }
}
