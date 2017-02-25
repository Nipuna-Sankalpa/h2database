package org.h2.server.web;

import org.h2.DBInternals.CustomConstants;
import org.h2.DBInternals.InsertMetaData;
import org.h2.engine.Constants;
import org.h2.engine.SysProperties;
import org.h2.index.Index;
import org.h2.server.web.WebApp;
import org.h2.server.web.WebServer;
import org.h2.util.New;
import org.h2.util.ScriptReader;

import java.io.StringReader;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Created by yellowflash on 2/23/17.
 */
public class CustomWebApp extends WebApp {
    public CustomWebApp(WebServer server) {
        super(server);
    }

    public String query() {
        String sql = attributes.getProperty("sql").trim();
        if (sql.toUpperCase().contains("INSERT")) {
            InsertMetaData.getInstance().drop = true;
        }
        try {
            ScriptReader r = new ScriptReader(new StringReader(sql));
            final ArrayList<String> list = New.arrayList();
            while (true) {
                String s = r.readStatement();
                if (s == null) {
                    break;
                }
                list.add(s);
            }
            final Connection conn = session.getConnection();
            if (SysProperties.CONSOLE_STREAM && server.getAllowChunked()) {
                String page = new String(server.getFile("result.jsp"), Constants.UTF8);
                int idx = page.indexOf("${result}");
                // the first element of the list is the header, the last the
                // footer
                list.add(0, page.substring(0, idx));

                list.add(page.substring(idx + "${result}".length()));
                session.put("chunks", new Iterator<String>() {
                    private int i, j, k = 0;
                    ArrayList<String> temp = null;

                    @Override
                    public boolean hasNext() {
                        return i < list.size();
                    }

                    @Override
                    public String next() {

                        String s = list.get(i++);
                        if (i == 1 || i == list.size()) {
                            return s;
                        }

                        if (s.toUpperCase().contains("INSERT") && temp != null) {
                            if (i == 3 && temp.size() > k) {
                                i--;
                                if (temp != null) {
                                    StringBuilder b = new StringBuilder();
                                    for (k = 0; k < temp.size(); k += 2) {
                                        s = temp.get(k);
                                        query(conn, s, k, temp.size() / 2, b);
                                    }
                                    j = 1;
                                    return b.toString();
                                }
                            }
                            if (i == list.size() - 1 && temp.size() > j) {
                                if (temp != null) {
                                    StringBuilder b = new StringBuilder();
                                    query(conn, s, i - 1, list.size() - 2, b);
                                    while (temp.size() > j) {
                                        s = temp.get(j);
                                        query(conn, s, j, temp.size() / 2, b);
                                        j += 2;
                                    }
                                    return b.toString();
                                }
                            }
                        }

                        StringBuilder b = new StringBuilder();
                        query(conn, s, i - 1, list.size() - 2, b);
                        if (InsertMetaData.getInstance().drop) {
                            temp = IndexAdjustment(list);
                            InsertMetaData.getInstance().drop = false;
                        }
                        return b.toString();
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                });
                return "result.jsp";
            }
            String result;
            StringBuilder buff = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                String s = list.get(i);
                query(conn, s, i, list.size(), buff);
            }
            result = buff.toString();
            session.put("result", result);
        } catch (Throwable e) {
            session.put("result", getStackTrace(0, e, session.getContents().isH2()));
        }
        return "result.jsp";
    }

    public ArrayList<String> IndexAdjustment(ArrayList<String> list) {


        InsertMetaData instance = InsertMetaData.getInstance();

        ConcurrentHashMap<String, Index> tempHashMap = instance.indexMap;
        String tableName = list.get(1).split(" ")[2];
        Index tempIndexInstance = null;
        ArrayList<String> indexListToBeUpdated = new ArrayList<>();
        String[] keyArray = !tempHashMap.isEmpty() ? tempHashMap.keySet().toArray(new String[tempHashMap.size()]) : null;

        if (keyArray != null) {
            for (int i = 0; i < keyArray.length; i++) {
                tempIndexInstance = tempHashMap.get(keyArray[i]);
                if (tempIndexInstance.getTable().getName().equals(tableName) && !keyArray[i].contains("PRIMARY") && !keyArray[i].contains("SYS")) {
                    String index = "DROP INDEX IF EXISTS " + keyArray[i];
                    indexListToBeUpdated.add(index);
                    index = "CREATE INDEX " + keyArray[i] + " ON " + tableName + "(" + tempIndexInstance.getColumns()[0] + ")";
                    indexListToBeUpdated.add(index);
                }
            }
        }

        int numberOfQueries = list.size() - 3;
        int numberOfIndexes = indexListToBeUpdated.size() / 2;

        if (numberOfQueries < CustomConstants.INSERT_QUERY_COUNT) {
            return null;
        }

        return indexListToBeUpdated;
    }


}
