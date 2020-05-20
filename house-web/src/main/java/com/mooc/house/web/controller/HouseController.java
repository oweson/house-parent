package com.mooc.house.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.mooc.house.biz.service.AgencyService;
import com.mooc.house.biz.service.CityService;
import com.mooc.house.biz.service.CommentService;
import com.mooc.house.biz.service.HouseService;
import com.mooc.house.biz.service.RecommendService;
import com.mooc.house.common.constants.CommonConstants;
import com.mooc.house.common.constants.HouseUserType;
import com.mooc.house.common.model.Comment;
import com.mooc.house.common.model.House;
import com.mooc.house.common.model.HouseUser;
import com.mooc.house.common.model.User;
import com.mooc.house.common.model.UserMsg;
import com.mooc.house.common.page.PageData;
import com.mooc.house.common.page.PageParams;
import com.mooc.house.common.result.ResultMsg;
import com.mooc.house.web.interceptor.UserContext;

@Controller
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private CityService cityService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private CommentService commentService;

    /**
     * 1.实现分页
     * 2.支持小区搜索、类型搜索
     * 3.支持排序
     * 4.支持展示图片、价格、标题、地址等信息
     *
     * @return
     */
    @RequestMapping("/house/list")
    public String houseList(Integer pageSize, Integer pageNum, House query, ModelMap modelMap) {
        // 1 房屋搜索
        PageData<House> ps = houseService.queryHouse(query, PageParams.build(pageSize, pageNum));
        // 2 热门房源，通过redis做计数器
        List<House> hotHouses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", hotHouses);
        modelMap.put("ps", ps);
        // 3 查询对象一并返回前端；
        modelMap.put("vo", query);
        return "house/listing";
    }

    /**
     * 2 新增之前的页面信息，不分信息回显
     */
    @RequestMapping("/house/toAdd")
    public String toAdd(ModelMap modelMap) {
        modelMap.put("citys", cityService.getAllCitys());
        // 前端添加模糊搜索，数据大的时候，返回前几个小区，其他的走搜索
        modelMap.put("communitys", houseService.getAllCommunitys());
        return "/house/add";
    }

    /**
     * 3 新增房源
     */
    @RequestMapping("/house/add")
    public String doAdd(House house) {
        // 添加人，未登陆回跳转登陆页面
        User user = UserContext.getUser();
        // 设置房屋的状态
        house.setState(CommonConstants.HOUSE_STATE_UP);
        houseService.addHouse(house, user);
        // 添加之后个人页面的模版页面
        return "redirect:/house/ownlist";
    }

    /**
     * 4 自己添加的房产列表
     */
    @RequestMapping("house/ownlist")
    public String ownlist(House house, Integer pageNum, Integer pageSize, ModelMap modelMap) {
        User user = UserContext.getUser();
        house.setUserId(user.getId());
        house.setBookmarked(false);
        modelMap.put("ps", houseService.queryHouse(house, PageParams.build(pageSize, pageNum)));
        // 区分售卖还是收藏
        modelMap.put("pageType", "own");
        return "/house/ownlist";
    }

    /**
     * 5 查询房屋详情
     * 查询关联经纪人
     *
     * @param id
     * @return
     */
    @RequestMapping("house/detail")
    public String houseDetail(Long id, ModelMap modelMap) {
        House house = houseService.queryOneHouse(id);
        HouseUser houseUser = houseService.getHouseUser(id);
        /*每点击一次就增加一次热度*/
        recommendService.increase(id);
        List<Comment> comments = commentService.getHouseComments(id, 8);
        // 判断null情况避免NPE
        if (houseUser.getUserId() != null && !houseUser.getUserId().equals(0)) {
            modelMap.put("agent", agencyService.getAgentDeail(houseUser.getUserId()));
        }
        List<House> rcHouses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", rcHouses);
        modelMap.put("house", house);
        modelMap.put("commentList", comments);
        return "/house/detail";
    }

    /**
     * 6 留言信息
     */
    @RequestMapping("house/leaveMsg")
    public String houseMsg(UserMsg userMsg) {
        houseService.addUserMsg(userMsg);
        return "redirect:/house/detail?id=" + userMsg.getHouseId() + ResultMsg.successMsg("留言成功").asUrlParams();
    }

    /**
     * 7 评分
     */
    @ResponseBody
    @RequestMapping("house/rating")
    public ResultMsg houseRate(Double rating, Long id) {
        houseService.updateRating(id, rating);
        return ResultMsg.successMsg("ok");
    }


    /**
     * 8 收藏,和售卖类似，只是类型不一样
     */
    @ResponseBody
    @RequestMapping("house/bookmark")
    public ResultMsg bookmark(Long id) {
        User user = UserContext.getUser();
        houseService.bindUser2House(id, user.getId(), true);
        return ResultMsg.successMsg("ok");
    }

    /**
     * 9 .删除收藏和收藏取反
     */
    @ResponseBody
    @RequestMapping("house/unbookmark")
    public ResultMsg unbookmark(Long id) {
        User user = UserContext.getUser();
        houseService.unbindUser2House(id, user.getId(), HouseUserType.BOOKMARK);
        return ResultMsg.successMsg("ok");
    }

    /**
     * 10 经纪人删除我的售卖房屋列表
     */
    @RequestMapping(value = "house/del")
    public String delsale(Long id, String pageType) {
        User user = UserContext.getUser();
        houseService.unbindUser2House(id, user.getId(), pageType.equals("own") ? HouseUserType.SALE : HouseUserType.BOOKMARK);
        return "redirect:/house/ownlist";
    }

    /**
     * 11 .收藏列表
     */
    @RequestMapping("house/bookmarked")
    public String bookmarked(House house, Integer pageNum, Integer pageSize, ModelMap modelMap) {
        User user = UserContext.getUser();
        house.setBookmarked(true);
        house.setUserId(user.getId());
        modelMap.put("ps", houseService.queryHouse(house, PageParams.build(pageSize, pageNum)));
        modelMap.put("pageType", "book");
        return "/house/ownlist";
    }
}
