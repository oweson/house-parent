package com.mooc.house.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mooc.house.common.model.Agency;
import com.mooc.house.common.model.User;
import com.mooc.house.common.page.PageParams;

@Mapper
public interface AgencyMapper {
    /**
     * 1 查询经纪人
     */
    List<Agency> select(Agency agency);

    /**
     * 2 新增经纪人
     */
    int insert(Agency agency);

    /**
     * 3 查询用户
     */
    List<User> selectAgent(@Param("user") User user, @Param("pageParams") PageParams pageParams);

    /**
     * 4 统计经纪人的数量
     */
    Long selectAgentCount(@Param("user") User user);

}
