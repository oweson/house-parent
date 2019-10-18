package com.mooc.house.util;

import lombok.Data;

import java.util.Date;

/**
 * the class is create by @Author:oweson
 *
 * @Date：2019/10/18 21:43
 */
@Data
public class Fish {
    private Integer id;
    private String name;
    private Date createTime;
    private Date updateTime;
}
