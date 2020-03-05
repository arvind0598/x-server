package com.blkx.server.models;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class DataSourceList {

    private Map<String, DataSource> map = new HashMap<>();

    private static DataSourceList instance = null;

    private DataSourceList() {}

    public static DataSourceList getInstance() {
        if (instance == null)
            instance = new DataSourceList();
        return instance;
    }

    public void put(String string, DataSource d) {
        map.put(string,d);
    }

    public Map<String, DataSource> getMap(){
        return map;
    }
}
