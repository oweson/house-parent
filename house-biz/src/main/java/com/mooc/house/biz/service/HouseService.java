package com.mooc.house.biz.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mooc.house.biz.mapper.HouseMapper;
import com.mooc.house.common.constants.HouseUserType;
import com.mooc.house.common.model.Community;
import com.mooc.house.common.model.House;
import com.mooc.house.common.model.HouseUser;
import com.mooc.house.common.model.User;
import com.mooc.house.common.model.UserMsg;
import com.mooc.house.common.page.PageData;
import com.mooc.house.common.page.PageParams;
import com.mooc.house.common.utils.BeanHelper;

@Service
public class HouseService {

    @Autowired
    private HouseMapper houseMapper;

    @Value("${file.prefix}")
    private String imgPrefix;

    @Autowired
    private FileService fileService;

    @Autowired
    private AgencyService agencyService;

    @Autowired
    private MailService mailService;


    /**
     * 1.查询小区
     * 2.添加图片服务器地址前缀
     * 3.构建分页结果
     *
     * @param query
     * @param build
     */
    public PageData<House> queryHouse(House query, PageParams pageParams) {
        List<House> houses = Lists.newArrayList();
        // 根据房屋的名字进行搜索
        if (!Strings.isNullOrEmpty(query.getName())) {
            Community community = new Community();
            community.setName(query.getName());
            List<Community> communities = houseMapper.selectCommunity(community);
            // 第一个小区和所有的小区代码复用，这里第一个！
            if (!communities.isEmpty()) {
                query.setCommunityId(communities.get(0).getId());
            }
        }
        // 返回房屋集合
        houses = queryAndSetImg(query, pageParams);
        //添加图片服务器地址前缀
        Long count = houseMapper.selectPageCount(query);
        // 查询房屋的总数
        return PageData.buildPage(houses, count, pageParams.getPageSize(), pageParams.getPageNum());
    }

    public List<House> queryAndSetImg(House query, PageParams pageParams) {
        List<House> houses = houseMapper.selectPageHouses(query, pageParams);
        // 设置主图，其他图片集合，户型图
        houses.forEach(h -> {
            h.setFirstImg(imgPrefix + h.getFirstImg());
            h.setImageList(h.getImageList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
            h.setFloorPlanList(h.getFloorPlanList().stream().map(img -> imgPrefix + img).collect(Collectors.toList()));
        });
        return houses;
    }

    public List<Community> getAllCommunitys() {
        Community community = new Community();
        return houseMapper.selectCommunity(community);
    }

    /**
     * 添加房屋图片 1 or many
     * 添加户型图片
     * 插入房产信息
     * 绑定用户到房产的关系
     *
     * @param house
     * @param user
     */
    public void addHouse(House house, User user) {
        // 图片是数组，转化字符串进行存储，逗号分割，因为数据库只存储路径，不存储文件和数据
        if (CollectionUtils.isNotEmpty(house.getHouseFiles())) {
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getHouseFiles()));
            house.setImages(images);
        }
        // 户型图
        if (CollectionUtils.isNotEmpty(house.getFloorPlanFiles())) {
            String images = Joiner.on(",").join(fileService.getImgPaths(house.getFloorPlanFiles()));
            house.setFloorPlan(images);
        }
        BeanHelper.onInsert(house);
        houseMapper.insert(house);
        // 绑定用户和房产的关系,false是添加房产，true是收藏房产
        // houseId是插入之后才有的，主键要设置自增返回主键！！！
        bindUser2House(house.getId(), user.getId(), false);
    }

    public void bindUser2House(Long houseId, Long userId, boolean collect) {
        HouseUser existhouseUser = houseMapper.selectHouseUser(userId, houseId, collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);
        // 对应关系已经存在就不操作了；
        if (existhouseUser != null) {
            return;
        }
        HouseUser houseUser = new HouseUser();
        houseUser.setHouseId(houseId);
        houseUser.setUserId(userId);
        houseUser.setType(collect ? HouseUserType.BOOKMARK.value : HouseUserType.SALE.value);
        // 插入之前设置默一些认值
        BeanHelper.setDefaultProp(houseUser, HouseUser.class);
        // 修改创建时间
        BeanHelper.onInsert(houseUser);
        // 入库
        houseMapper.insertHouseUser(houseUser);
    }

    public HouseUser getHouseUser(Long houseId) {
        HouseUser houseUser = houseMapper.selectSaleHouseUser(houseId);
        return houseUser;
    }

    public House queryOneHouse(Long id) {
        House query = new House();
        query.setId(id);
        List<House> houses = queryAndSetImg(query, PageParams.build(1, 1));
        if (!houses.isEmpty()) {
            return houses.get(0);
        }
        return null;
    }

    public void addUserMsg(UserMsg userMsg) {
        // 设置默认值。如Integer，字符串为空等
        BeanHelper.onInsert(userMsg);
        houseMapper.insertUserMsg(userMsg);
        // 找到经纪人对象；
        User agent = agencyService.getAgentDeail(userMsg.getAgentId());
        mailService.sendMail("来自用户" + userMsg.getEmail() + "的留言", userMsg.getMsg(), agent.getEmail());
    }

    public void updateRating(Long id, Double rating) {
        House house = queryOneHouse(id);
        Double oldRating = house.getRating();
        // 防止其他的用户不用ajax,非法请求，大于5就是非法的
        Double newRating = oldRating.equals(0D) ? rating : Math.min((oldRating + rating) / 2, 5);
        House updateHouse = new House();
        updateHouse.setId(id);
        updateHouse.setRating(newRating);
        BeanHelper.onUpdate(updateHouse);
        houseMapper.updateHouse(updateHouse);
    }

    public void unbindUser2House(Long id, Long userId, HouseUserType type) {
        if (type.equals(HouseUserType.SALE)) {
            houseMapper.downHouse(id);
        } else {
            houseMapper.deleteHouseUser(id, userId, type.value);
        }

    }

}
