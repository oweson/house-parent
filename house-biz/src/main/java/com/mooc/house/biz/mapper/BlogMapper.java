package com.mooc.house.biz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.mooc.house.common.model.Blog;
import com.mooc.house.common.page.PageParams;

@Mapper
public interface BlogMapper {
    /**
     * 1 查询博客
     */
    List<Blog> selectBlog(@Param("blog") Blog query, @Param("pageParams") PageParams params);

    /**
     * 2 统计博客的数量
     */
    Long selectBlogCount(Blog query);

}
