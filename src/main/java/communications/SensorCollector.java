package communications;

import com.google.gson.Gson;
import core.ReasonerInput;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.BluetoothManager;
import utils.DharmaProperties;
import utils.PaeManager;
import utils.SocialManager;
import utils.UsbManager;

/**
 * This class callects all data from sensors (logs and anomalies), appends them
 * and send the anomalies detected to the event correlator
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class SensorCollector implements Runnable {

    private static Gson gson = new Gson();
    private static ArrayList<String> paths;
    private static HashMap<String, Object> anomalies = new HashMap<>();
    private static final DharmaProperties props = new DharmaProperties();

    public SensorCollector(String inputsFile) {
        FileReader fileReader = null;
        try {
            Thread.sleep(1000);
            fileReader = new FileReader(inputsFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            List<String> lines = new ArrayList<>();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
            bufferedReader.close();
            paths = new ArrayList<String>(Arrays.asList(lines.toArray(new String[lines.size()])));
        } catch (Exception ex) {
            Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fileReader.close();
            } catch (IOException ex) {
                Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void run() {
        WatchService watcher;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            String[] input = new String(Files.readAllBytes(Paths.get(props.getAnomalyPathsValue()))).split("\r");

            for (String inputItem : input) {
                inputItem = inputItem.substring(inputItem.indexOf("///"));
                File file = new File(inputItem);
                if (file.isDirectory()) {
                    Paths.get(inputItem).register(watcher, ENTRY_MODIFY, ENTRY_CREATE);
                } else {
                    Paths.get(file.getParent().toString()).register(watcher, ENTRY_MODIFY, ENTRY_CREATE);
                }
            }

            while (true) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException ex) {
                    return;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == ENTRY_MODIFY || kind == ENTRY_CREATE) {
                        SensorCollector.processData();
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }

                Thread.sleep(1000);
            }

        } catch (Exception ex) {
            Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * En función del sensor, manda el dato recibido al procesador
     * correspondiente
     */
    private static void processData() {
        for (String path : paths) {
            String item = path.split("///")[0];
            String path_ = path.split("///")[1];
            switch (item) {
                case "SOCIAL":
                    new SocialManager(path_);
                    break;
                case "BLUETOOTH":
                    new BluetoothManager(path_);
                    break;
                case "USB":
                    new UsbManager(path_);
                    break;
                case "PAE":
                    new PaeManager(path_);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Una vez procesado el dato del sensor, se vuelve a tratar para enviarlo al
     * correlador de eventos y se añade al conjunto de datos recibidos
     *
     * @param input dato procesado en hashmap
     */
    public static void receiveNewData(HashMap<String, HashMap<String, Object>> input) {

        String event = input.entrySet().iterator().next().getKey();
        HashMap infoMap = gson.fromJson(input.entrySet().iterator().next().getValue().toString(), HashMap.class);
        String info = gson.toJson(infoMap);
        long timestamp = System.currentTimeMillis();

        if (event.equals("Correlator")) {
            //CHECK!
            Iterator<Map.Entry<String, Object>> entries = infoMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                if (!((entry.getValue().equals("{}") || entry.getKey().equals("Time") || entry.getKey().equals("Date")))) {
                    HashMap output = new HashMap();
                    output.put("Anomaly", infoMap.get(entry.getKey()));
                    output.put("Time", infoMap.get("Time"));
                    output.put("Date", infoMap.get("Date"));
                    sendToCorrelator("Correlator-" + entry.getKey() + "///" + gson.toJson(output) + "///" + timestamp);
                }
            }
        } else if (event.equals("PAE")) {
            Iterator<Map.Entry<String, Object>> entries = infoMap.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, Object> entry = entries.next();
                if (!((entry.getValue().equals("{}") || entry.getKey().equals("Time") || entry.getKey().equals("Date")))) {
                    HashMap output = new HashMap();
                    output.put("Anomaly", infoMap.get(entry.getKey()));
                    output.put("Time", infoMap.get("Time"));
                    output.put("Date", infoMap.get("Date"));
                    sendToCorrelator("PAE-" + entry.getKey() + "///" + gson.toJson(output) + "///" + timestamp);
                }
            }

        } else {
            sendToCorrelator(event + "///" + info + "///" + timestamp);
        }

        anomalies.putAll(input);
        anomalies.put("Timestamp", timestamp);
        String jsonString = gson.toJson(anomalies);
        ReasonerInput.newDataMap(gson.fromJson(jsonString, HashMap.class));

    }

    /**
     * Envía el dato al correlador de eventos a través de un fichero
     *
     * @param string dato
     */
    private static void sendToCorrelator(String string) {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(props.getSECInputFileValue(), true)));
            writer.println(string);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(SensorCollector.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            writer.close();
        }
    }
}
