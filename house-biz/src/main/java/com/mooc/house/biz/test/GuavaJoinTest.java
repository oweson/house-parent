package com.mooc.house.biz.test;

import com.google.common.base.Joiner;

/**
 * the class is create by @Author:oweson
 *
 * @Date：2019/8/31 7:05
 */
public class GuavaJoinTest {
    public static void main(String[] args) {
        Object[] arr = {1,2,3,4,5};
        String join = Joiner.on(",").join(arr);
        System.out.println(join);
    }
}
