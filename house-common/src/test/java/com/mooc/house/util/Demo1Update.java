package com.mooc.house.util;

import com.mooc.house.common.utils.BeanHelper;

/**
 * the class is create by @Author:oweson
 *
 * @Date：2019/10/18 21:43
 */
public class Demo1Update {
    public static void main(String[] args) {
        Fish fish = new Fish();
        BeanHelper.onInsert(fish);
        BeanHelper.setDefaultProp(fish, Fish.class);
        System.out.println(fish);
        // ""和" "是不一样的！！！
        System.out.println(fish.getName().equals(" "));

    }
}
