package com.mooc.house.web.controller;

import com.google.common.base.Objects;
import com.mooc.house.biz.service.AgencyService;
import com.mooc.house.biz.service.HouseService;
import com.mooc.house.biz.service.MailService;
import com.mooc.house.biz.service.RecommendService;
import com.mooc.house.common.constants.CommonConstants;
import com.mooc.house.common.model.Agency;
import com.mooc.house.common.model.House;
import com.mooc.house.common.model.User;
import com.mooc.house.common.page.PageData;
import com.mooc.house.common.page.PageParams;
import com.mooc.house.common.result.ResultMsg;
import com.mooc.house.web.interceptor.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class AgencyController {

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private RecommendService recommendService;

    @Autowired
    private HouseService houseService;

    @Autowired
    private MailService mailService;

    @RequestMapping("agency/create")
    public String agencyCreate() {
        User user = UserContext.getUser();
        if (user == null || !Objects.equal(user.getEmail(), "spring_boot@163.com")) {
            return "redirect:/accounts/signin?" + ResultMsg.successMsg("请先登录").asUrlParams();
        }
        return "/user/agency/create";
    }

    /**
     * 2 经纪人列表
     */
    @RequestMapping("/agency/agentList")
    public String agentList(Integer pageSize, Integer pageNum, ModelMap modelMap) {
        if (pageSize == null) {
            // 默认值
            pageSize = 6;
        }
        PageData<User> ps = agencyService.getAllAgent(PageParams.build(pageSize, pageNum));
        // 推荐热门房产
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", houses);
        modelMap.put("ps", ps);
        return "/user/agent/agentList";
    }

    /**
     * 3 经纪人详情
     */
    @RequestMapping("/agency/agentDetail")
    public String agentDetail(Long id, ModelMap modelMap) {
        User user = agencyService.getAgentDeail(id);
        // 热门的房产
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        // 经纪人下的房产信息
        House query = new House();
        query.setUserId(id);
        // todo?
        query.setBookmarked(false);
        // 查询并分页
        PageData<House> bindHouse = houseService.queryHouse(query, new PageParams(3, 1));
        // 经纪人旗下的房产
        if (bindHouse != null) {
            modelMap.put("bindHouses", bindHouse.getList());
        }
        // 推荐热门的房产
        modelMap.put("recomHouses", houses);
        // 经纪人
        modelMap.put("agent", user);
        // 经纪机构的名字
        modelMap.put("agencyName", user.getAgencyName());
        return "/user/agent/agentDetail";
    }

    /**
     * 4 留言给经纪人
     */
    @RequestMapping("/agency/agentMsg")
    public String agentMsg(Long id, String msg, String name, String email, ModelMap modelMap) {
        User user = agencyService.getAgentDeail(id);
        mailService.sendMail("咨询", "email:" + email + ",msg:" + msg, user.getEmail());
        return "redirect:/agency/agentDetail?id=" + id + "&" + ResultMsg.successMsg("留言成功").asUrlParams();
    }

    /**
     * 5 经纪机构详情
     */
    @RequestMapping("/agency/agencyDetail")
    public String agencyDetail(Integer id, ModelMap modelMap) {
        Agency agency = agencyService.getAgency(id);
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", houses);
        modelMap.put("agency", agency);
        return "/user/agency/agencyDetail";
    }

    /**
     * 6 经纪机构列表
     */
    @RequestMapping("agency/list")
    public String agencyList(ModelMap modelMap) {
        List<Agency> agencies = agencyService.getAllAgency();
        // 没分页？？？
        List<House> houses = recommendService.getHotHouse(CommonConstants.RECOM_SIZE);
        modelMap.put("recomHouses", houses);
        modelMap.put("agencyList", agencies);
        return "/user/agency/agencyList";
    }

    /**
     * 7 添加经纪机构
     */
    @RequestMapping("agency/submit")
    public String agencySubmit(Agency agency) {
        User user = UserContext.getUser();
        if (user == null || !Objects.equal(user.getEmail(), "spring_boot@163.com")) {
            //只有超级管理员可以添加,拟定spring_boot@163.com为超管
            return "redirect:/accounts/signin?" + ResultMsg.successMsg("请先登录").asUrlParams();
        }
        agencyService.add(agency);
        return "redirect:/index?" + ResultMsg.successMsg("创建成功").asUrlParams();
    }


}
