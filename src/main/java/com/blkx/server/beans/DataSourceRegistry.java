package com.blkx.server.beans;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DataSourceRegistry {
    private Map<String, DataSource> map;

    public DataSourceRegistry() {
        map = new HashMap<>();
    }

    public void registerDataSource(String name, DataSource dataSource) {
        map.put(name, dataSource);
    }

    public List<String> getAllSources() {
        return List.copyOf(map.keySet());
    }

    public Optional<DataSource> getDataSource(String name) {
        return Optional.ofNullable(map.get(name));
    }
}
