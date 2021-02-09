package com.hornetmall.processor.test;

import com.hornetmall.framework.annotation.SecurityResource;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "user")
@Table(name = "t_user")
@SecurityResource(
        name = "DATA_USER",
        columns = {"userId", "userName"}
)
public class User {
    @Id
    private String userId;
    private String userName;
}
