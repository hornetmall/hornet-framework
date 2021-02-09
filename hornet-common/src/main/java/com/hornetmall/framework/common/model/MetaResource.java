package com.hornetmall.framework.common.model;

import lombok.Data;

@Data
public class MetaResource {

    private String name;
    private String displayName;
    private MetaTable table;
    private String type;
}
