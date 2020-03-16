package com.blkx.server.models;

public class GenerateRequestModel {
    private String tableName;
    private String columnName;
    private Boolean hasParent;
    private Boolean hasChildren;

    public GenerateRequestModel(String tableName, String columnName, Boolean hasParent, Boolean hasChildren) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.hasParent = hasParent;
        this.hasChildren = hasChildren;
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
