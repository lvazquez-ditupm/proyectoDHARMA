package utils;

import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * This class creates a global config file with active and non-active periods,
 * in order to differentiate log
 *
 * @author UPM (member of DHARMA Development Team) (http://dharma.inf.um.es)
 * @version 1.0
 */
public class ActivePeriods {

    private static final DharmaProperties props = new DharmaProperties();

    public static void create() {
             
        HashMap<Integer, String> weekMap = new HashMap<>();
        HashMap<String, Object> jsonMap = new HashMap<>();

        Gson gson = new Gson();

        String raw_ = props.getActivePeriodsValue();
        raw_ = raw_.substring(1, raw_.length() - 1);
        String[] raw = raw_.split("\\)\\(");
        String[] yearPeriods = raw[0].split(";");
        String[] weekPeriods = raw[1].split(";");

        for (int i = 1; i <= 7; i++) {

            for (String weekPeriod : weekPeriods) {

                int dayStart = Character.getNumericValue(weekPeriod.split(">")[0].charAt(0));
                int dayEnd = Character.getNumericValue(weekPeriod.split(">")[0].charAt(2));

                if (dayStart <= i && i <= dayEnd) {
                    weekMap.put(i, weekPeriod.split(">")[1]);
                    break;
                }
            }
        }

        jsonMap.put("Year", yearPeriods);
        jsonMap.put("Week", weekMap);

        PrintWriter writer;
        try {
            writer = new PrintWriter(props.getDatasetPathValue()+"/global.dharma", "UTF-8");
            writer.println(gson.toJson(jsonMap));
            writer.close();
            System.out.println("****  Generado fichero global.dharma  ****");
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(ActivePeriods.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
