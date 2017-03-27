package communications;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.BluetoothManager;
import utils.DharmaProperties;
import utils.PaeManager;
import utils.SocialManager;
import utils.TsusenManager;
import utils.UsbManager;

/**
 *
 * @author root
 */
public class SensorCollector implements Runnable {

    private static Gson gson = new Gson();
    private static ArrayList<String> paths;
    private static HashMap<String, Object> anomalies = new HashMap<>();
    private static final DharmaProperties props = new DharmaProperties();

    public SensorCollector(String inputsFile) {
        FileReader fileReader = null;
        try {
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
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;
                    Path fileName = ev.context();

                    System.out.println(kind.name() + ": " + fileName);

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

    private static void processData() {
        for (String path : paths) {
            String item = path.split("///")[0];
            String path_ = path.split("///")[1];
            switch (item) {
                case "TSUSEN":
                    new TsusenManager(path_);
                    break;
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

    public static void receiveNewData(HashMap<String, HashMap<String, Object>> input) {

        anomalies.putAll(input);

        String jsonString = gson.toJson(anomalies);

    }

}
