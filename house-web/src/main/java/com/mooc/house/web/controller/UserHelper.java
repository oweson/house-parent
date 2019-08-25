package com.mooc.house.web.controller;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Objects;
import com.mooc.house.common.model.User;
import com.mooc.house.common.result.ResultMsg;

public class UserHelper {
    /**
     * 1 校验表单数据是否合法
     */
    public static ResultMsg validate(User accout) {
        if (StringUtils.isBlank(accout.getEmail())) {
            return ResultMsg.errorMsg("Email 有误");
        }
        if (StringUtils.isBlank(accout.getEmail())) {
            return ResultMsg.errorMsg("Email 有误");
        }
        if (StringUtils.isBlank(accout.getConfirmPasswd()) || StringUtils.isBlank(accout.getPasswd())
                || !accout.getPasswd().equals(accout.getConfirmPasswd())) {
            return ResultMsg.errorMsg("Email 有误");
        }
        if (accout.getPasswd().length() < 6) {
            return ResultMsg.errorMsg("密码大于6位");
        }
        return ResultMsg.successMsg("");
    }

    /**
     * 2 重置密码的校验
     */
    public static ResultMsg validateResetPassword(String key, String password, String confirmPassword) {
        if (StringUtils.isBlank(key) || StringUtils.isBlank(password) || StringUtils.isBlank(confirmPassword)) {
            return ResultMsg.errorMsg("参数有误");
        }
        if (!Objects.equal(password, confirmPassword)) {
            return ResultMsg.errorMsg("密码必须与确认密码一致");
        }
        return ResultMsg.successMsg("");
    }

}
