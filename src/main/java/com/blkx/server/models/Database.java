package com.blkx.server.models;

import org.springframework.stereotype.Component;

public class Database {
    private String userName;
    private String dbName;
    private String password;
    private String url;

    public Database(String userName, String dbName, String password, String url) {
        this.userName = userName;
        this.dbName = dbName;
        this.password = password;
        this.url = url;
    }

    public Database() {
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
