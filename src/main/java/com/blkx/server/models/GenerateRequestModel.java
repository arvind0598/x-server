package com.blkx.server.models;

public class GenerateRequestModel {
    private String tableName;
    private String columnName;
    private String option;
    private String value;
    private String field;
    private String generatedQuery;

    public String getGeneratedQuery() {
        return generatedQuery;
    }

    public void setGeneratedQuery(String generatedQuery) {
        this.generatedQuery = generatedQuery;
    }

    public GenerateRequestModel(String tableName, String columnName, String option, String value, String field) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.option = option;
        this.value = value;
        this.field = field;
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

    public String getOption() { return option; }

    public void setOption(String option) { this.option = option; }

    public String getValue() { return value; }

    public void setValue(String value) { this.value = value; }

    public String getField() { return field; }

    public void setField(String field) { this.field = field; }

    @Override
    public String toString() {
        return "GenerateRequestModel{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                '}';
    }
}
