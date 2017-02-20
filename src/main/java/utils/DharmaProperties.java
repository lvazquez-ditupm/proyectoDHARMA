package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * This class allows to get the required configuration parameters to run the
 * evaluationSystemExecutor package.
 *
 * @author UPM (member of DHARMA Development Team)(http://dharma.inf.um.es)
 * @version 1.0
 */
public class DharmaProperties {

    private static Properties dharmaproperties;

    private static String DHARMA_PROPERTIES_FILE;

    private static final String DHARMA_PATH_PROP = "dharma.uri";
    private static final String BAG_VISUALIZATOR_PATH_PROP = "bag.visualizator.uri";
    private static final String JSON_PATH_PROP = "json.uri";

    //Valores de los parametros del fichero properties
    private static String DHARMA_PATH_VALUE;
    private static String BAG_VISUALIZATOR_PATH_VALUE;
    private static String JSON_PATH_VALUE;

    public DharmaProperties() {
        dharmaproperties = new Properties();
        InputStream is = null;
        try {
            String configFile = "dharma.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            DHARMA_PROPERTIES_FILE = (new File(path).getParentFile().getParentFile().getPath() + File.separator + configFile).toString();
            File f = new File(DHARMA_PROPERTIES_FILE);
            is = new FileInputStream(f);
            dharmaproperties.load(is);
            DHARMA_PATH_VALUE = dharmaproperties.getProperty(DHARMA_PATH_PROP);
            BAG_VISUALIZATOR_PATH_VALUE = dharmaproperties.getProperty(BAG_VISUALIZATOR_PATH_PROP);
            JSON_PATH_VALUE = dharmaproperties.getProperty(JSON_PATH_PROP);
            
        } catch (IOException e) {
            throw new RuntimeException("Could not read config module config. (File: " + DHARMA_PROPERTIES_FILE + ")", e);
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public String getDharmaPathValue() {
        return DHARMA_PATH_VALUE;
    }

    public String getBagVisualizatorPathValue() {
        return BAG_VISUALIZATOR_PATH_VALUE;
    }

    public String getJSONPathValue() {
        return JSON_PATH_VALUE;
    }
}
