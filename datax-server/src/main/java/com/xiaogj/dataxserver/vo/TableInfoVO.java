package com.xiaogj.dataxserver.vo;

import java.io.Serializable;

public class TableInfoVO implements Serializable {
    private static final long serialVersionUID = -6817182189774934885L;

    private String name;
    private int order;

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
