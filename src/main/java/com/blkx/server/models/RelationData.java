package com.blkx.server.models;

public class RelationData {

    private String constraintName;
    private String destTable;
    private String destColumn;

    public RelationData(String constraintName, String destTable, String destColumn) {
        this.constraintName = constraintName;
        this.destTable = destTable;
        this.destColumn = destColumn;
    }

    public String getConstraintName() {
        return constraintName;
    }

    public void setConstraintName(String constraintName) {
        this.constraintName = constraintName;
    }

    public String getDestTable() {
        return destTable;
    }

    public void setDestTable(String destTable) {
        this.destTable = destTable;
    }

    public String getDestColumn() {
        return destColumn;
    }

    public void setDestColumn(String destColumn) {
        this.destColumn = destColumn;
    }

    @Override
    public String toString() {
        return "RelationData{" +
                "constraintName='" + constraintName + '\'' +
                ", destTable='" + destTable + '\'' +
                ", destColumn='" + destColumn + '\'' +
                '}';
    }
}
