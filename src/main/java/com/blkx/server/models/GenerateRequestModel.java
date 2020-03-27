package com.blkx.server.models;

public class GenerateRequestModel {
    private String tableName;
    private String columnName;
    private Boolean hasParent;
    private Boolean hasChildren;
    private String option;
    private String value;
    private String field;

    public GenerateRequestModel(String tableName, String columnName, Boolean hasParent, Boolean hasChildren, String option, String value, String field) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.hasParent = hasParent;
        this.hasChildren = hasChildren;
        this.option = option;
        this.value = value;
        this.field = field;

    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public GenerateRequestModel() {
    }

    public String getOption() {
        return option;
    }

    public void setOption(String option) {
        this.option = option;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public Boolean getHasParent() {
        return hasParent;
    }

    public void setHasParent(Boolean hasParent) {
        this.hasParent = hasParent;
    }

    public Boolean getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(Boolean hasChildren) {
        this.hasChildren = hasChildren;
    }

    @Override
    public String toString() {
        return "GenerateRequestModel{" +
                "tableName='" + tableName + '\'' +
                ", columnName='" + columnName + '\'' +
                ", hasParent=" + hasParent +
                ", hasChildren=" + hasChildren +
                '}';
    }
}
