package com.xiaogj.dataxserver.vo;

import java.io.Serializable;
import java.sql.Date;

public class SyncVO implements Serializable {
    private static final long serialVersionUID = -2747671096968558284L;
    private int id;
    private int dbId;
    private int tableId;
    private Date serviceTimeSync;
    private Date endTime;
    private boolean isInsert;

    public boolean isInsert() {
        return isInsert;
    }

    public void setInsert(boolean insert) {
        isInsert = insert;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDbId() {
        return dbId;
    }

    public void setDbId(int dbId) {
        this.dbId = dbId;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public Date getServiceTimeSync() {
        return serviceTimeSync;
    }

    public void setServiceTimeSync(Date serviceTimeSync) {
        this.serviceTimeSync = serviceTimeSync;
    }
}
