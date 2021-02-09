package com.hornetmall.processor.test;

import com.hornetmall.framework.annotation.SecurityModule;
import com.hornetmall.framework.annotation.SecurityOperation;

@SecurityModule("User")
public class UserService {

    @SecurityOperation(name = "addUser")
    public void test() {
    }

    @SecurityOperation()
    public void updateUser() {
    }
}
