package com.xiaogj.dataxserver.vo;

import java.io.Serializable;

public class TableInfoVO implements Serializable {
    private static final long serialVersionUID = -6817182189774934885L;

    private int id;
    private String name;
    private int order;
    private String querySql;
    private String createSql;
    private String writeColumn;
    private SyncVO syncVO;

    public SyncVO getSyncVO() {
        return syncVO;
    }

    public void setSyncVO(SyncVO syncVO) {
        this.syncVO = syncVO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWriteColumn() {
        return writeColumn;
    }

    public void setWriteColumn(String writeColumn) {
        this.writeColumn = writeColumn;
    }

    public String getQuerySql() {
        return querySql;
    }

    public void setQuerySql(String querySql) {
        this.querySql = querySql;
    }

    public String getCreateSql() {
        return createSql;
    }

    public void setCreateSql(String createSql) {
        this.createSql = createSql;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}
