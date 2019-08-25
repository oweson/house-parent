package com.mooc.house.common.model;


import java.util.Date;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class User {

    private Long id;

    private String email;

    private String phone;

    private String name;

    private String passwd;

    private String confirmPasswd;

    private Integer type;
    //普通用户1，经纪人2

    private Date createTime;

    private Integer enable;

    private String avatar;

    private MultipartFile avatarFile;

    private String newPassword;

    private String key;

    private Long agencyId;

    private String aboutme;

    private String agencyName;


}
