package utils;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * This class parses Suricata logs, creating a hashmap from a string
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SuricataEventParser {

    public static HashMap<String, String> parseIDS(String event) {

        LinkedHashMap<String, String> eventMap = new LinkedHashMap<>();

        eventMap.put("event", event.substring(0, event.indexOf("[") - 1));
        event = event.substring(event.indexOf("]") + 3);
        eventMap.put("classification", event.substring(event.indexOf(":") + 2, event.indexOf("]")));
        event = event.substring(event.indexOf("]") + 3);
        eventMap.put("priority", event.substring(event.indexOf(":") + 2, event.indexOf("]")));
        event = event.substring(event.indexOf("]") + 3);
        eventMap.put("protocol", event.substring(0, event.indexOf("}")));
        event = event.substring(event.indexOf("}") + 2);
        eventMap.put("ip_src", event.substring(0, event.indexOf(":")));
        event = event.substring(event.indexOf(":") + 1);
        eventMap.put("port_src", event.substring(0, event.indexOf(" ")));
        event = event.substring(event.indexOf("->") + 3);
        eventMap.put("ip_dst", event.substring(0, event.indexOf(":")));
        event = event.substring(event.indexOf(":") + 1);
        eventMap.put("port_dst", event);

        return eventMap;
    }

    /**
     * Parsea el string definido y devuelve la anomalía y un hashmap con los
     * datos importantes relacionados
     *
     * @param event string recibido
     * @return anomalía + hashmap
     */
    public static Object[] parseAnomaly(String event) {
        Object[] out = new Object[2];
        String[] aux = event.split(";");
        HashMap<String, String> parseMap = new HashMap<>();
        for (int i = 1; i < aux.length; i++) {
            String key = aux[i].split("\":\"")[0];
            String value = aux[i].split("\":\"")[1];
            key = key.substring(1);
            value = value.substring(0, value.length() - 1);
            parseMap.put(key, value);
        }
        out[0] = aux[0];
        out[1] = parseMap;
        return out;
    }
}
