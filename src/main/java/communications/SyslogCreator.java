package communications;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.DharmaProperties;
import utils.SuricataEventParser;

/**
 * This class generates Syslog entries based on logs received
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SyslogCreator {

	private final DharmaProperties props = new DharmaProperties();
	private static final Logger logger = LoggerFactory.getLogger(SyslogCreator.class);

	ArrayList<HashMap<String, Date>> receivedEvents;
	Date refDate;
	int period;

	public SyslogCreator() {
		receivedEvents = new ArrayList<>();
		period = Integer.parseInt(props.getSyslogPeriodValue());
	}

	/**
	 * Detecta si el log es de anomalía de red o evento detectado
	 *
	 * @param log
	 *            log recibido
	 *
	 */
	public void put(String log) {
		if (log.contains("Network ANOMALY of:")) {
			setAnomaly(Integer.parseInt(log.substring(log.indexOf(":") + 2, log.indexOf("/"))));
		} else if (log.contains("[**]")) {
			setIDSLog(log);
		} else {
			setAnomalyLog(log);
		}
	}

	/**
	 * Genera un log con el nivel de anomalía de red actual
	 *
	 * @param anomaly
	 *            anomalía recibida del IDS
	 */
	private void setAnomaly(Integer anomaly) {
		if (anomaly >= Integer.parseInt(props.getAnomalyThresholdValue())) {
			logger.info("Nivel de Anomalía: " + anomaly + " %");
		}
	}

	/**
	 * Si dos eventos del IDS llegan durante una ventana temporal, se comprobará
	 * que no sean eventos repetidos. Si no lo son, se parsean y se procesan.
	 *
	 * @param log
	 *            evento recibido
	 */
	private void setIDSLog(String log) {
		String dateStr = log.substring(0, log.indexOf("."));
		SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
		HashMap<String, Date> event = new HashMap<>();

		if (receivedEvents.isEmpty()) {
			try {
				Date currDate = format.parse(dateStr);
				refDate = currDate;
				event.put(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2), currDate);
				receivedEvents.add(event);
				proceedIDS(SuricataEventParser.parseIDS(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2)));
			} catch (ParseException ex) {
				java.util.logging.Logger.getLogger(SyslogCreator.class.getName()).log(Level.SEVERE, null, ex);
			}

		} else {

			try {
				Date currDate = format.parse(dateStr);
				while (currDate.getTime() - refDate.getTime() > period * 1000) {
					receivedEvents.remove(0);
					if (!receivedEvents.isEmpty()) {
						refDate = (Date) receivedEvents.get(0).values().toArray()[0];
					} else {
						refDate = currDate;
						event.put(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2), currDate);
						receivedEvents.add(event);
						proceedIDS(SuricataEventParser
								.parseIDS(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2)));
						return;
					}
				}

				if (detectRepeatedIDSEvent(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2))) {
					event.put(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2), currDate);
					receivedEvents.add(event);
				} else {
					proceedIDS(SuricataEventParser.parseIDS(log.substring(log.indexOf("]", log.indexOf("]") + 1) + 2)));
				}

			} catch (ParseException ex) {
				java.util.logging.Logger.getLogger(SyslogCreator.class.getName()).log(Level.SEVERE, null, ex);
			}

		}
	}

	/**
	 * Parsea el log recibido proveniente del detector de anomalías
	 *
	 * @param event
	 *            evento recibido
	 */
	private void setAnomalyLog(String event) {
		proceedAnomaly(SuricataEventParser.parseAnomaly(event));
	}

	/**
	 * Genera el evento en syslog a partir del evento IDS recibido
	 *
	 * @param event
	 *            evento recibido
	 */
	private void proceedIDS(HashMap<String, String> eventReceived) {

		String out = "IDSAnomaly: ";
		Iterator it = eventReceived.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			out += e.getKey() + "=" + e.getValue() + "; ";
		}

		logger.info(out);
	}

	/**
	 * Genera el evento en syslog a partir del evento recibido del correlador
	 *
	 * @param event
	 *            evento recibido
	 */
	private void proceedAnomaly(Object[] aux) {

		String out = (String) aux[0] + ": ";
		HashMap<String, String> eventReceived = (HashMap<String, String>) aux[1];
		Iterator it = eventReceived.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			out += e.getKey() + "=" + e.getValue() + "; ";
		}

		logger.info(out);
	}

	/**
	 * Compara parámetros de los eventos del IDS para detectar si provienen de
	 * un mismo ataque
	 *
	 * @param event_
	 *            evento recibido
	 * @return boolean indicando si es o no repetido
	 */
	private boolean detectRepeatedIDSEvent(String event_) {
		HashMap<String, String> eventReceivedParsed = SuricataEventParser.parseIDS(event_);
		HashMap<String, String> eventSavedParsed;
		String[] keys;
		boolean event = false;
		boolean classification = false;
		boolean priority = false;
		boolean protocol = false;
		boolean ip_src = false;
		boolean port_src = false;
		boolean ip_dst = false;
		boolean port_dst = false;

		for (HashMap<String, Date> receivedEvent : receivedEvents) {

			eventSavedParsed = SuricataEventParser.parseIDS((String) receivedEvent.keySet().toArray()[0]);

			event = eventSavedParsed.get("event").equals(eventReceivedParsed.get("event"));
			classification = eventSavedParsed.get("classification").equals(eventReceivedParsed.get("classification"));
			priority = eventSavedParsed.get("priority").equals(eventReceivedParsed.get("priority"));
			protocol = eventSavedParsed.get("protocol").equals(eventReceivedParsed.get("protocol"));
			ip_src = eventSavedParsed.get("ip_src").equals(eventReceivedParsed.get("ip_src"));
			port_src = eventSavedParsed.get("port_src").equals(eventReceivedParsed.get("port_src"));
			ip_dst = eventSavedParsed.get("ip_dst").equals(eventReceivedParsed.get("ip_dst"));
			port_dst = eventSavedParsed.get("port_dst").equals(eventReceivedParsed.get("port_dst"));

			if (event && priority) {
				return true;
			} else if (classification && protocol && ip_src && port_src) {
				return true;
			} else if (classification && protocol && ip_dst && port_dst) {
				return true;
			} else if (classification && protocol && ip_src && ip_dst) {
				return true;
			}
		}

		return false;
	}
}
