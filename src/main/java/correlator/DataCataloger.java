package correlator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.gson.Gson;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import utils.DharmaProperties;

/**
 *
 * This class sets a data type of each parameter of each sensor log (help to
 * correlator)
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class DataCataloger implements Runnable {

    private HashMap<String, String> timestampDict;

    public DataCataloger() {
    }

    private static final DharmaProperties props = new DharmaProperties();
    List<File> directories = new ArrayList<>();

    public void run() {

        File f = new File(props.getDatasetPathValue());
        FileWriter newFile = null;
        PrintWriter pw = null;
        File file;
        String dirPath;
        String filePath = null;
        int numSensors = 0;
        timestampDict = parseConfig(props.getDatasetTimestampsValue());
        System.out.println("****  Detectando directorios con datasets "
                + "y generando ficheros de configuración ****");

        while (true) {
            getDirectories(f);
            if (numSensors != directories.size()) {
                numSensors = directories.size();
                for (int i = 0; i < numSensors; i++) {
                    dirPath = directories.get(i).getAbsolutePath();
                    BufferedReader datos = null;
                    try {
                        String[] header;
                        String[] log;
                        LinkedHashMap<String, String> jsonHeader;
                        String json;

                        for (int j = 0; j < directories.get(i).listFiles().length; j++) {
                            File file_ = directories.get(i).listFiles()[j];
                            if (!file_.getName().endsWith(".dharma") && file_.isFile()) {
                                filePath = directories.get(i).listFiles()[j].getAbsolutePath();
                                break;
                            }
                        }

                        if (filePath != null) {

                            datos = new BufferedReader(new FileReader(filePath));
                            header = datos.readLine().split(",");
                            log = datos.readLine().split(",");
                            jsonHeader = catalog(log, header);
                            json = generateJSON(jsonHeader);

                            PrintWriter writer = new PrintWriter(dirPath + "/config.dharma", "UTF-8");
                            writer.println(json);
                            writer.close();
                            filePath = null;

                        }
                    } catch (Exception e) {
                    }
                }
            }
            try {
                Thread.sleep(600000);
                System.out.println("**** Revisando datasets de nuevos sensores ****");
            } catch (InterruptedException ex) {
                Logger.getLogger(DataCataloger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Detecta si cada elemento es numérico o textual y genera un nuevo header
     * con los tipos
     *
     * @param log línea de log a analizar
     * @param oldHeader cabeceras sin tipos
     * @return nueva cabecera
     */
    private LinkedHashMap<String, String> catalog(String[] log, String[] oldHeader) {

        LinkedHashMap<String, String> newHeader = new LinkedHashMap<>();
        LinkedHashMap<String, String> times = new LinkedHashMap<>();

        for (int i = 0; i < oldHeader.length; i++) {
            if (timestampDict.containsKey(oldHeader[i])) {
                newHeader.put(oldHeader[i], catalogTime(log[i], oldHeader[i]));
                times.put(oldHeader[i], catalogTime(log[i], oldHeader[i]));
            } else if (log[i].matches("-?\\d+(\\.\\d+)?")) {
                newHeader.put(oldHeader[i], "numerical");
            } else {
                newHeader.put(oldHeader[i], "nominal");
            }
        }
        String mainTime = getMainTime(times);

        if (mainTime != null) {
            for (Map.Entry<String, String> entry : newHeader.entrySet()) {
                if (entry.getKey().equals(mainTime)) {
                    newHeader.put(entry.getKey(), entry.getValue() + "*");
                }
            }
        }

        return newHeader;
    }

    /**
     * Si un mismo nombre de timestamp corresponde a varios formatos, se intenta
     * detectar el correcto. Si hubiera varios se termina el programa con
     * excepción
     *
     * @param timestamp valor de timestamp
     * @param timestampID nombre del timestamp
     * @return tipo de timestamp
     */
    private String catalogTime(String timestamp, String timestampID) {

        String type = timestampDict.get(timestampID);
        String[] types;
        String candidate = "";
        Date current;
        Date reference;
        Date date;

        if (type.startsWith("(") || type.startsWith("[") || type.startsWith("{")) {
            type = type.substring(1, type.length() - 1);
            types = type.split(",");
            SimpleDateFormat refFormat = new SimpleDateFormat("yyyy");
            current = new Date();
            try {
                reference = refFormat.parse("1");
                Boolean flag = false;
                for (String typeItem : types) {
                    SimpleDateFormat format = new SimpleDateFormat(typeItem);
                    try {
                        date = format.parse(timestamp);
                    } catch (ParseException e) {
                        continue;
                    }
                    if (flag) {
                        System.err.println("Se han definido varios timestamps "
                                + "con el mismo nombre (" + timestampID + ") y distinto "
                                + "formato (" + type + "), y es imposible diferenciarlos.");
                        System.err.println("Cambie los nombres correspondientes y reinicie DHARMA.");
                        System.exit(0);
                    } else if (current.after(date) && reference.before(date)) {
                        flag = true;
                        candidate = typeItem;
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(DataCataloger.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            candidate = type;
        }

        return candidate;
    }

    /**
     * Devuelve una lista con todos los directorios existentes en uno dado
     *
     * @param root directorio raiz
     */
    private void getDirectories(File root) {

        File[] files = root.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                directories.add(file);
                getDirectories(file);
            }
        }
    }

    /**
     * Genera un fichero JSON con los nombres de las variables, los tipos y si
     * son o no timestamps (con su tipo)
     *
     * @param jsonHeader información del dataset para generar el JSON
     * @return JSON
     */
    private String generateJSON(LinkedHashMap<String, String> jsonHeader) {

        Gson gson = new Gson();

        LinkedHashMap<String, Object> jsonMap = new LinkedHashMap<>();

        LinkedHashMap<String, String> typesMap = new LinkedHashMap<>();
        LinkedHashMap<String, String> timesMap = new LinkedHashMap<>();

        ArrayList<String> namesList = new ArrayList<>();
        ArrayList<LinkedHashMap> typesList = new ArrayList<>();
        ArrayList<LinkedHashMap> timesList = new ArrayList<>();
        String mainTime = "";

        for (Map.Entry<String, String> header : jsonHeader.entrySet()) {
            namesList.add(header.getKey());
            if (header.getValue().contains("numerical")
                    || header.getValue().contains("nominal")) {
                timesMap.put(header.getKey(), "Not a timestamp");
                typesMap.put(header.getKey(), header.getValue());
            } else if (header.getValue().contains("*")) {
                mainTime = header.getKey();
                timesMap.put(header.getKey(), header.getValue().substring(
                        0, header.getValue().length() - 1));
                typesMap.put(header.getKey(), "numerical");
            } else {
                timesMap.put(header.getKey(), header.getValue());
                typesMap.put(header.getKey(), "numerical");
            }
        }

        typesList.add(typesMap);
        timesList.add(timesMap);

        jsonMap.put("names", namesList);
        jsonMap.put("types", typesList);
        jsonMap.put("times", timesList);
        jsonMap.put("mainTime", mainTime);

        return gson.toJson(jsonMap);

    }

    /**
     * Parsea el string de configuración para crear un diccionario de timestamps
     * con tipos
     *
     * @param timestamps string del fichero de configuración
     * @return diccionario
     */
    private HashMap<String, String> parseConfig(String timestamps) {

        HashMap<String, String> dictionary = new HashMap<>();
        String[] timestampsArray = timestamps.split(";");

        for (String timestamp : timestampsArray) {
            dictionary.put(timestamp.split(":")[0], timestamp.split(":")[1]);
        }
        return dictionary;
    }

    /**
     * Si un dataset tiene varios parámetros temporales, se clasifica el
     * timestamp principal como el que más información temporal tiene
     *
     * @param times timestamps del dataset
     * @return timestamp principal
     */
    private String getMainTime(HashMap<String, String> times) {

        String main = null;

        if (times.size() == 1) {
            for (Map.Entry<String, String> entry : times.entrySet()) {
                return entry.getKey();
            }
        } else if (times.size() > 1) {

            int yearValue = 7;
            int monthValue = 6;
            int weekValue = 5;
            int dayValue = 4;
            int hourValue = 3;
            int minuteValue = 1;
            int secondValue = 1;
            int millisecondValue = 1;

            int maxSum = 0;

            for (Map.Entry<String, String> entry : times.entrySet()) {

                int sum = 0;

                if (entry.getValue().contains("y")) {
                    sum += yearValue;
                }
                if (entry.getValue().contains("M")) {
                    sum += monthValue;
                }
                if (entry.getValue().contains("w") || entry.getValue().contains("W")) {
                    sum += weekValue;
                }
                if (entry.getValue().contains("d") || entry.getValue().contains("D")
                        || entry.getValue().contains("E")) {
                    sum += dayValue;
                }
                if (entry.getValue().contains("H") || entry.getValue().contains("k")
                        || entry.getValue().contains("K") || entry.getValue().contains("h")) {
                    sum += hourValue;
                }
                if (entry.getValue().contains("m")) {
                    sum += minuteValue;
                }
                if (entry.getValue().contains("s")) {
                    sum += secondValue;
                }
                if (entry.getValue().contains("S")) {
                    sum += millisecondValue;
                }

                if (sum > maxSum) {
                    maxSum = sum;
                    main = entry.getKey();
                }
            }
        }

        return main;

    }
}
