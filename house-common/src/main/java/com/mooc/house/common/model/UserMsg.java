package com.mooc.house.common.model;

import lombok.Data;

import java.util.Date;

@Data
public class UserMsg {

    private Long id;
    private String msg;
    private Long userId;
    private Date createTime;
    // 经纪人id
    private Long agentId;
    // 房屋id
    private Long houseId;
    private String email;

    private String userName;


}
