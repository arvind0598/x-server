package com.blkx.server.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class ConfigService {

    private Map<UUID, String> queryTable = new HashMap<>();

    public UUID insertNewQuery(String query) {
        UUID uuid = UUID.randomUUID();
        queryTable.put(uuid, query);
        return uuid;
    }

    public String getQuery(UUID uuid) {
        return queryTable.getOrDefault(uuid, null);
    }
}
