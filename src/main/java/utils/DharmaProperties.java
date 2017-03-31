package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * This class allows to get the required configuration parameters to run DHARMA
 *
 * @author UPM (member of DHARMA Development Team)(http://dharma.inf.um.es)
 * @version 1.0
 */
public class DharmaProperties {

    private static Properties dharmaproperties;

    private static String DHARMA_PROPERTIES_FILE;

    private static final String DHARMA_PATH_PROP = "dharma.uri";
    private static final String GRAPH_VISUALIZATOR_PATH_PROP = "graph.visualizator.uri";
    private static final String JSON_PATH_PROP = "json.uri";
    private static final String EVENT_LOG_PATH_PROP = "event.log.uri";
    private static final String DATASET_PATH_PROP = "dataset.uri";
    private static final String DATASET_TIMESTAMPS_PROP = "dataset.timestamps";
    private static final String TIMESTAMP_REF_PROP = "timestamp.ref";
    private static final String ACTIVE_PERIODS_PROP = "active.periods";
    private static final String ANOMALY_THRESHOLD_PROP = "anomaly.threshold";
    private static final String SYSLOG_PERIOD_PROP = "syslog.period";
    private static final String ANOMALY_PATHS_PROP = "anomaly.paths";
    private static final String SOCIAL_THRESHOLD_PROP = "social.threshold";
    private static final String SEC_INPUT_FILE_PROP = "sec.input.file";

    //Valores de los parametros del fichero properties
    private static String DHARMA_PATH_VALUE;
    private static String GRAPH_VISUALIZATOR_PATH_VALUE;
    private static String JSON_PATH_VALUE;
    private static String EVENT_LOG_PATH_VALUE;
    private static String DATASET_PATH_VALUE;
    private static String DATASET_TIMESTAMPS_VALUE;
    private static String TIMESTAMP_REF_VALUE;
    private static String ACTIVE_PERIODS_VALUE;
    private static String ANOMALY_THRESHOLD_VALUE;
    private static String SYSLOG_PERIOD_VALUE;
    private static String ANOMALY_PATHS_VALUE;
    private static String SOCIAL_THRESHOLD_VALUE;
    private static String SEC_INPUT_FILE_VALUE;

    public DharmaProperties() {
        dharmaproperties = new Properties();
        InputStream is = null;
        try {
            String configFile = "config_files/dharma.conf";
            String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
            path = URLDecoder.decode(path, "UTF-8");
            DHARMA_PROPERTIES_FILE = (new File(path).getParentFile().getParentFile().getPath() + File.separator + configFile);
            File f = new File(DHARMA_PROPERTIES_FILE);
            is = new FileInputStream(f);
            dharmaproperties.load(is);
            DHARMA_PATH_VALUE = dharmaproperties.getProperty(DHARMA_PATH_PROP);
            GRAPH_VISUALIZATOR_PATH_VALUE = dharmaproperties.getProperty(GRAPH_VISUALIZATOR_PATH_PROP);
            JSON_PATH_VALUE = dharmaproperties.getProperty(JSON_PATH_PROP);
            EVENT_LOG_PATH_VALUE = dharmaproperties.getProperty(EVENT_LOG_PATH_PROP);
            DATASET_PATH_VALUE = dharmaproperties.getProperty(DATASET_PATH_PROP);
            DATASET_TIMESTAMPS_VALUE = dharmaproperties.getProperty(DATASET_TIMESTAMPS_PROP);
            TIMESTAMP_REF_VALUE = dharmaproperties.getProperty(TIMESTAMP_REF_PROP);
            ACTIVE_PERIODS_VALUE = dharmaproperties.getProperty(ACTIVE_PERIODS_PROP);
            ANOMALY_THRESHOLD_VALUE = dharmaproperties.getProperty(ANOMALY_THRESHOLD_PROP);
            SYSLOG_PERIOD_VALUE = dharmaproperties.getProperty(SYSLOG_PERIOD_PROP);
            ANOMALY_PATHS_VALUE = dharmaproperties.getProperty(ANOMALY_PATHS_PROP);
            SOCIAL_THRESHOLD_VALUE = dharmaproperties.getProperty(SOCIAL_THRESHOLD_PROP);
            SEC_INPUT_FILE_VALUE = dharmaproperties.getProperty(SEC_INPUT_FILE_PROP);
            validate();
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

    private void validate() {
        StringWriter swError = new StringWriter();
        PrintWriter pwError = new PrintWriter(swError);
        if (null == DHARMA_PATH_VALUE) {
            System.out.println("File \"" + DHARMA_PATH_PROP
                    + "\" not defined.");
        }
        if (null == GRAPH_VISUALIZATOR_PATH_VALUE) {
            System.out.println("Property \"" + GRAPH_VISUALIZATOR_PATH_PROP
                    + "\" not defined.");
        }
        if (null == JSON_PATH_VALUE) {
            System.out.println("Property \"" + JSON_PATH_PROP
                    + "\" not defined.");
        }
        if (null == EVENT_LOG_PATH_VALUE) {
            System.out.println("Property \"" + EVENT_LOG_PATH_PROP
                    + "\" not defined.");
        }
        if (null == DATASET_PATH_VALUE) {
            System.out.println("Property \"" + DATASET_PATH_PROP
                    + "\" not defined.");
        }
        if (null == DATASET_TIMESTAMPS_VALUE) {
            System.out.println("Property \"" + DATASET_TIMESTAMPS_PROP
                    + "\" not defined.");
        }
        if (null == TIMESTAMP_REF_VALUE) {
            System.out.println("Property \"" + TIMESTAMP_REF_PROP
                    + "\" not defined.");
        }
        if (null == ACTIVE_PERIODS_VALUE) {
            System.out.println("Property \"" + ACTIVE_PERIODS_PROP
                    + "\" not defined.");
        }
        if (null == ANOMALY_THRESHOLD_VALUE) {
            System.out.println("Property \"" + ANOMALY_THRESHOLD_PROP
                    + "\" not defined.");
        }
        if (null == SYSLOG_PERIOD_VALUE) {
            System.out.println("Property \"" + SYSLOG_PERIOD_PROP
                    + "\" not defined.");
        }
        if (null == ANOMALY_PATHS_VALUE) {
            System.out.println("Property \"" + ANOMALY_PATHS_PROP
                    + "\" not defined.");
        }
        if (null == SOCIAL_THRESHOLD_VALUE) {
            System.out.println("Property \"" + SOCIAL_THRESHOLD_PROP
                    + "\" not defined.");
        }
        if (null == SEC_INPUT_FILE_VALUE) {
            System.out.println("Property \"" + SEC_INPUT_FILE_PROP
                    + "\" not defined.");
        }
    }

    public String getDharmaPathValue() {
        return DHARMA_PATH_VALUE;
    }

    public String getGraphVisualizatorPathValue() {
        return GRAPH_VISUALIZATOR_PATH_VALUE;
    }

    public String getJSONPathValue() {
        return JSON_PATH_VALUE;
    }

    public String getEventLogPathValue() {
        return EVENT_LOG_PATH_VALUE;
    }

    public String getDatasetPathValue() {
        return DATASET_PATH_VALUE;
    }

    public String getDatasetTimestampsValue() {
        return DATASET_TIMESTAMPS_VALUE;
    }

    public String getTimestampReferenceValue() {
        return TIMESTAMP_REF_VALUE;
    }

    public String getActivePeriodsValue() {
        return ACTIVE_PERIODS_VALUE;
    }

    public String getAnomalyThresholdValue() {
        return ANOMALY_THRESHOLD_VALUE;
    }

    public String getSyslogPeriodValue() {
        return SYSLOG_PERIOD_VALUE;
    }

    public String getAnomalyPathsValue() {
        return ANOMALY_PATHS_VALUE;
    }

    public String getSocialThresholdValue() {
        return SOCIAL_THRESHOLD_VALUE;
    }

    public String getSECInputFileValue() {
        return SEC_INPUT_FILE_VALUE;
    }
}
