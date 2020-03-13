package com.mooc.house.web.controller.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * the class is create by @Author:oweson
 *
 * @Date：2020/1/30 17:53
 */
@RestController
public class DemoController {
    @RequestMapping("npe")
    public Object npe() {
        String s = null;
        s.length();
        return "";
    }
}
