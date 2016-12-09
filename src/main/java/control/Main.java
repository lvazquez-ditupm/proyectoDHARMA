package control;

import communications.LogReceiver;
import core.BAG;

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

		logReceiver = new LogReceiver(6000, "127.0.0.1");
		logReceiver.start();

	}
}
