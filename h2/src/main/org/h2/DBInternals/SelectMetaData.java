package org.h2.DBInternals;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by yellowflash on 2/24/17.
 */
public class SelectMetaData {
    private SelectMetaData() {
    }

    private static SelectMetaData instance = null;
    Map<String, Map<String, Double>> statMap = new HashMap<>();


    public static SelectMetaData getInstance() {
        if (instance == null) {
            instance = new SelectMetaData();
        }
        return instance;
    }

}
