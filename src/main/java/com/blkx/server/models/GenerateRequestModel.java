package com.blkx.server.models;

public class GenerateRequestModel {
    private String tableName;
    private String columnName;

    public GenerateRequestModel(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public GenerateRequestModel() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    @Override
    public String toString() {
        return "GenerateRequestModel{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
