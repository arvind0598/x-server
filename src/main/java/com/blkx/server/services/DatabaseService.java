package com.blkx.server.services;

import com.blkx.server.config.DatasourceConfig;
import com.blkx.server.models.DataSourceList;
import com.blkx.server.models.TableMetaData;
import org.postgresql.util.PSQLException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
public class DatabaseService {

//    @Autowired
    private DataSource dataSource;
    private DataSourceList dataSourceList = DataSourceList.getInstance();

//    @Autowired
//    public DatabaseService(DataSource dataSource) {
//        this.dataSource = dataSource;
//    }

//    move this method somewhere else
    private String getType(Integer num) {
        Map<Integer, String> map = new HashMap<>();
        map.put(4, "INTEGER");
        map.put(12, "VARCHAR");
        return map.getOrDefault(num, "OTHER");
    }

//    change driver class name to a map
    public void postCredentials(String url, String username, String password) throws SQLException {
        System.out.println(url + username + password);
        Connection connection = dataSource.getConnection();
        if(connection != null) connection.close();
    }

    public List<String> getTableNames() throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            DatabaseMetaData data = connection.getMetaData();
            String[] fetchTypes = {"TABLE", "VIEW"};
            ResultSet resultSet = data.getTables(connection.getCatalog(), "public", "%", fetchTypes);
            List<String> tableNames = new ArrayList<>();

            while(resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                tableNames.add(tableName);
            }

            connection.close();
            return tableNames;
        }
    }

    public TableMetaData getTableData(String tableName) throws SQLException {
        try(Connection connection = dataSource.getConnection()) {
            DatabaseMetaData data = connection.getMetaData();
            String[] fetchTypes = {"TABLE", "VIEW"};
            ResultSet resultSet = data.getColumns(connection.getCatalog(), "public", tableName, null);
            TableMetaData metaData = new TableMetaData(tableName);

            while(resultSet.next()) {
                String columnName = resultSet.getString(4);
                String columnType = getType(resultSet.getInt(5));
                metaData.addColumn(columnName, columnType);
            }

            connection.close();
            return metaData;
        }
    }

    public void setActiveDataSource(String database){
        dataSource = dataSourceList.getMap().get(database);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
