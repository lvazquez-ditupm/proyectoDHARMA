package control;

import communications.LogReceiver;
import communications.SECEventReceiver;
import core.BAG;

/**
 * This class starts the system
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class Main {

	private static LogReceiver logReceiver;
	///
	// private static MarkovController markovController;
	///

	public static void main(String[] args) {

		BAG.exportCleanJSON();
		///
		// markovController = new MarkovController();
		// markovController.parse("HMM: IDAtaque=97;TipoAtaque=Ataque multipaso
		/// con
		/// persistencia;Nodos=(BeEF,Reversing,Sudo,Filtración,Pivoting,Acceso
		/// Servidor,Persistencia);Estado=Sudo;PEstado=0.42;PFinal=0.4");
		// markovController.parse("HMM:
		/// IDAtaque=\"1\";TipoAtaque=\"DDOS\";Nodos=\"D2,D3,A2,N2\";Estado=\"D3\";PEstado=\"0.3\";PFFinal=\"0.4\"");
		// markovController.parse("HMM:
		/// IDAtaque=\"1\";TipoAtaque=\"DDOS\";Nodos=\"D2,D3,A2,N2\";Estado=\"N2\";PEstado=\"0.3\";PFFinal=\"0.4\"");
		///

		logReceiver = new LogReceiver(6000, "192.168.10.100");
		logReceiver.start();

	}
}
