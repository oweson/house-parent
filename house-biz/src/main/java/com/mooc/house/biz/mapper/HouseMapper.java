package com.mooc.house.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mooc.house.common.model.Community;
import com.mooc.house.common.model.House;
import com.mooc.house.common.model.HouseUser;
import com.mooc.house.common.model.User;
import com.mooc.house.common.model.UserMsg;
import com.mooc.house.common.page.PageParams;

@Mapper
public interface HouseMapper {

    List<House> selectPageHouses(@Param("house") House house, @Param("pageParams") PageParams pageParams);

    Long selectPageCount(@Param("house") House query);

    int insert(User account);

    List<Community> selectCommunity(Community community);

    int insert(House house);

    HouseUser selectHouseUser(@Param("userId") Long userId, @Param("id") Long houseId, @Param("type") Integer integer);

    HouseUser selectSaleHouseUser(@Param("id") Long houseId);

    int insertHouseUser(HouseUser houseUser);

    int insertUserMsg(UserMsg userMsg);

    int updateHouse(House updateHouse);

    int downHouse(Long id);

    int deleteHouseUser(@Param("id") Long id, @Param("userId") Long userId, @Param("type") Integer value);

}
