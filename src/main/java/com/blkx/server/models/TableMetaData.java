package com.blkx.server.models;

import java.util.ArrayList;
import java.util.List;

public class TableMetaData {
    private String name;
    private List<ColumnMetaData> columns;

    public TableMetaData(String name) {
        this.name = name;
        this.columns = new ArrayList<>();
    }

    public List<ColumnMetaData> getColumns() {
        return columns;
    }

    public void addColumn(ColumnMetaData columnMetaData) {
        columns.add(columnMetaData);
    }

    public void addColumn(String columnName, String columnType) {
        columns.add(new ColumnMetaData(columnName, columnType));
    }

    @Override
    public String toString() {
        return "TableMetaData{" +
                "columns=" + columns +
                '}';
    }
}
