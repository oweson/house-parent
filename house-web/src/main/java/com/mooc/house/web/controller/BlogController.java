package com.mooc.house.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mooc.house.biz.service.BlogService;
import com.mooc.house.biz.service.CommentService;
import com.mooc.house.biz.service.RecommendService;
import com.mooc.house.common.constants.CommonConstants;
import com.mooc.house.common.model.Blog;
import com.mooc.house.common.model.Comment;
import com.mooc.house.common.model.House;
import com.mooc.house.common.page.PageData;
import com.mooc.house.common.page.PageParams;

@Controller
public class BlogController {

    @Autowired
    private BlogService blogService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RecommendService recommendService;

    /**
     * 1 博客列表
     */
    @RequestMapping(value = "blog/list", method = {RequestMethod.POST, RequestMethod.GET})
    public String list(Integer pageSize, Integer pageNum, Blog query, ModelMap modelMap) {
        PageData<Blog> ps = blogService.queryBlog(query, PageParams.build(pageSize, pageNum));
        // 热门房源
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", houses);
        modelMap.put("ps", ps);
        return "/blog/listing";
    }

    /**
     * 2 博客详情
     */
    @RequestMapping(value = "blog/detail", method = {RequestMethod.POST, RequestMethod.GET})
    public String blogDetail(int id, ModelMap modelMap) {
        Blog blog = blogService.queryOneBlog(id);
        // 显示8条评论
        List<Comment> comments = commentService.getBlogComments(id, 8);
        // 热门房源
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", houses);
        modelMap.put("blog", blog);
        modelMap.put("commentList", comments);
        return "/blog/detail";
    }
}
