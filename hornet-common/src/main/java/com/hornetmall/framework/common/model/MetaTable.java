package com.hornetmall.framework.common.model;

import lombok.Data;

import java.util.List;

@Data
public class MetaTable {
    private String name;
    private String displayName;
    private List<MetaColumn> columns;
}
