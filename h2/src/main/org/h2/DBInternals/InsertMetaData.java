package org.h2.DBInternals;

import org.h2.command.dml.Insert;
import org.h2.index.Index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yellowflash on 2/24/17.
 */
public class InsertMetaData {
    private static InsertMetaData instance;
    public boolean drop = true;
    public ConcurrentHashMap<String, Index> indexMap = null;

    private InsertMetaData() {
    }

    public static InsertMetaData getInstance() {
        if (instance == null) {
            instance = new InsertMetaData();
        }
        return instance;
    }

    public ArrayList<Double> executedTimeList = new ArrayList<>();
}
