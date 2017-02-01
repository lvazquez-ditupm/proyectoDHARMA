package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class parses each single line of the SEC event log, to find what's the
 * next node to advance in the BAG
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SECEventParser {

    /**
     * Extrae los distintos datos del log de SEC recibido
     *
     * @param str log de SEC
     * @return hashmap con datos clasificados
     */
    public static HashMap<String, Object> parse(String str) {

        HashMap<String, Object> nodeData = new HashMap<>();
        HashMap<String, Object> outData = new HashMap<>();

        if (str.contains("EVENTO: ")) {

            System.out.println(str);

            String dateStr = str.substring(4, str.indexOf("EVENTO: ") - 2);
            SimpleDateFormat format = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
            Date date;
            try {
                date = format.parse(dateStr);
                nodeData.put("date", date);
            } catch (ParseException ex) {
                Logger.getLogger(SECEventParser.class.getName()).log(Level.SEVERE, null, ex);
            }

            str = str.substring(str.indexOf("EVENTO: ") + 8);

            String event = str.substring(0, str.indexOf("(") - 1);
            nodeData.put("event", event);

            String detail = str.substring(str.indexOf("(") + 1, str.indexOf(")"));
            nodeData.put("detail", detail);

            outData = process(nodeData);
        }

        if (!outData.isEmpty()) {
            //TODO
        }

        return outData;
    }

    /**
     * Procesa los detalles del log, detectando los parámetros de origen y de
     * destino
     *
     * @param nodeData hashmap sin procesar
     * @return hashmap con clasificación origen-destino
     */
    private static HashMap<String, Object> process(HashMap<String, Object> nodeData) {

        String detail = (String) nodeData.get("detail");
        HashMap<String, String> sourceMap = new HashMap<>();
        HashMap<String, String> destinationMap = new HashMap<>();

        String eventDetail = detail.substring(0, detail.indexOf(";"));
        String[] data = detail.substring(detail.indexOf(";") + 2).split("; ");

        nodeData.remove("detail");

        for (String dataItem : data) {
            String[] dataItemArray = dataItem.split(": ");
            if (dataItemArray[0].contains("src")) {
                sourceMap.put(dataItemArray[0].substring(0, dataItemArray[0].length() - 4), dataItemArray[1]);
            } else if (dataItemArray[0].contains("dst")) {
                destinationMap.put(dataItemArray[0].substring(0, dataItemArray[0].length() - 4), dataItemArray[1]);
            } else {
                System.err.println("Error en definición de parámetros en SEC. No se sabe si es origen o destino.");
                System.err.println(dataItem);
                System.err.println("Se finaliza la ejecución del sistema DHARMA");
                System.exit(0);
            }
        }

        if (!sourceMap.isEmpty()) {
            nodeData.put("sourceMap", sourceMap);
        }
        if (!destinationMap.isEmpty()) {
            nodeData.put("destinationMap", destinationMap);
        }
        return nodeData;
    }
}
