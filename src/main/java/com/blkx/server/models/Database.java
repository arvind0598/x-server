package com.blkx.server.models;

import org.springframework.stereotype.Component;

public class Database {
    private String userName;
    private String datasourceName;
    private String password;
    private String datasourceUrl;

    public Database(String userName, String datasourceName, String password, String datasourceUrl) {
        this.userName = userName;
        this.datasourceName = datasourceName;
        this.password = password;
        this.datasourceUrl = datasourceUrl;
    }

    public Database() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatasourceUrl() {
        return datasourceUrl;
    }

    public void setDatasourceUrl(String datasourceUrl) {
        this.datasourceUrl = datasourceUrl;
    }
}
