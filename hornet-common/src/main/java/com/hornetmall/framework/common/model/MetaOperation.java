package com.hornetmall.framework.common.model;

import lombok.Data;

import java.util.List;

@Data
public class MetaOperation {

    private String name;
    private String module;
    private String className;
    private String methodName;
    private List<String> references;
}
