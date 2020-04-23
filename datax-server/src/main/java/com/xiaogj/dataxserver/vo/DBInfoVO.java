package com.xiaogj.dataxserver.vo;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class DBInfoVO implements Serializable {

    private static final long serialVersionUID = -7912057106880308905L;
    private int id;
    private String name;
    private String username;
    private String password;
    private String jdbcUrl;
    private Date baseServiceTime;
    private List<SyncVO> syncList = new ArrayList<>();
    private List<String> jsonPathList = new ArrayList<>();

    public void addJsonPathList(String jsonPath) {
        jsonPathList.add(jsonPath);
    }

    public List<String> getJsonPathList() {
        return jsonPathList;
    }

    public void setJsonPathList(List<String> jsonPathList) {
        this.jsonPathList = jsonPathList;
    }

    public void addSyncList(SyncVO syncVO) {
        syncList.add(syncVO);
    }

    public List<SyncVO> getSyncList() {
        return syncList;
    }

    public void setSyncList(List<SyncVO> syncList) {
        this.syncList = syncList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Date getBaseServiceTime() {
        return baseServiceTime;
    }

    public void setBaseServiceTime(Date baseServiceTime) {
        this.baseServiceTime = baseServiceTime;
    }
}
