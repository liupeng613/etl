package com.xiaogj.dataxserver.vo;

import java.io.Serializable;

public class OperateTargetSqlVO implements Serializable {
    private static final long serialVersionUID = 7213199068883114732L;
    private int id;
    private int index;
    private int type;
    private String operateSql;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getOperateSql() {
        return operateSql;
    }

    public void setOperateSql(String operateSql) {
        this.operateSql = operateSql;
    }
}
