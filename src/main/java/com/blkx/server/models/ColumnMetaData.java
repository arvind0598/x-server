package com.blkx.server.models;

public class ColumnMetaData {
    private String name;
    private String type;

    public ColumnMetaData(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ColumnMetaData{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
