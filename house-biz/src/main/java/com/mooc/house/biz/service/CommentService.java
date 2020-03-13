package com.mooc.house.biz.service;

import java.util.List;

import com.mooc.house.common.constants.CommentTypeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mooc.house.biz.mapper.CommentMapper;
import com.mooc.house.common.constants.CommonConstants;
import com.mooc.house.common.model.Comment;
import com.mooc.house.common.model.User;
import com.mooc.house.common.utils.BeanHelper;

@Service
public class CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserService userService;

    // 通用评论设计
    public void addHouseComment(Long houseId, String content, Long userId) {
        addComment(houseId, null, content, userId, 1);
    }

    // todo 私有的方法会让事务不生效
    @Transactional(rollbackFor = Exception.class)
    private void addComment(Long houseId, Integer blogId, String content, Long userId, int type) {
        Comment comment = new Comment();
        if (type == CommentTypeConstants.BLOG) {
            comment.setHouseId(houseId);
        } else {
            comment.setBlogId(blogId);
        }
        comment.setContent(content);
        comment.setUserId(userId);
        comment.setType(type);
        // 1 更新时间
        BeanHelper.onInsert(comment);
        // 2 设置默认值
        BeanHelper.setDefaultProp(comment, Comment.class);
        commentMapper.insert(comment);
    }

    public List<Comment> getHouseComments(long houseId, int size) {
        List<Comment> comments = commentMapper.selectComments(houseId, size);
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setAvatar(user.getAvatar());
            comment.setUserName(user.getName());
        });
        return comments;
    }

    public List<Comment> getBlogComments(long blogId, int size) {
        List<Comment> comments = commentMapper.selectBlogComments(blogId, size);
        comments.forEach(comment -> {
            User user = userService.getUserById(comment.getUserId());
            comment.setUserName(user.getName());
            comment.setAvatar(user.getAvatar());
        });
        return comments;
    }

    public void addBlogComment(int blogId, String content, Long userId) {
        addComment(null, blogId, content, userId, CommonConstants.COMMENT_BLOG_TYPE);
    }


}
