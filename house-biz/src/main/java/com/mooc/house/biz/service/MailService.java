package com.mooc.house.biz.service;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.mooc.house.biz.mapper.UserMapper;
import com.mooc.house.common.model.User;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    /**
     * 发送邮件的来源，事先存在的
     */
    @Value("${spring.mail.username}")
    private String from;


    @Value("${domain.name}")
    private String domainName;


    @Autowired
    private UserMapper userMapper;

    /**
     * 1 缓存，同时注册100用户，超过剔除！
     * 缓存15min
     */
    private final Cache<String, String> registerCache =
            CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(15, TimeUnit.MINUTES)
                    .removalListener(new RemovalListener<String, String>() {
                        // removeListener 15min不激活，数据库删除这个用户！防止唯一键冲突，下次注册；

                        @Override
                        public void onRemoval(RemovalNotification<String, String> notification) {
                            String email = notification.getValue();
                            User user = new User();
                            user.setEmail(email);
                            List<User> targetUser = userMapper.selectUsersByQuery(user);
                            if (!targetUser.isEmpty() && Objects.equal(targetUser.get(0).getEnable(), 0)) {
                                // 过期删除
                                userMapper.delete(email);
                                // 代码优化: 在删除前首先判断用户是否已经被激活，对于未激活的用户进行移除操作
                            }

                        }
                    }).build();


    private final Cache<String, String> resetCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(15, TimeUnit.MINUTES).build();

    /**
     * 异步发送邮件；
     * @Async:会吧这个任务放到线程池里面
     */
    @Async
    public void sendMail(String title, String url, String email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setSubject(title);
        message.setTo(email);
        message.setText(url);
        mailSender.send(message);
    }

    /**
     * 1.缓存key-email的关系 2.借助spring mail 发送邮件 3.借助异步框架进行异步操作
     *
     * @param email
     */
    @Async
    public void registerNotify(String email) {
        String randomKey = RandomStringUtils.randomAlphabetic(10);
        registerCache.put(randomKey, email);
        String url = "http://" + domainName + "/accounts/verify?key=" + randomKey;
        sendMail("房产平台激活邮件", url, email);
    }

    /**
     * 发送重置密码邮件
     *
     * @param email
     */
    @Async
    public void resetNotify(String email) {
        String randomKey = RandomStringUtils.randomAlphanumeric(10);
        resetCache.put(randomKey, email);
        String content = "http://" + domainName + "/accounts/reset?key=" + randomKey;
        sendMail("房产平台密码重置邮件", content, email);
    }

    public String getResetEmail(String key) {
        return resetCache.getIfPresent(key);
    }

    public void invalidateRestKey(String key) {
        resetCache.invalidate(key);
    }

    public boolean enable(String key) {
        // 从缓存拿到key对应的值，是否存在且正确！
        String email = registerCache.getIfPresent(key);
        if (StringUtils.isBlank(email)) {
            return false;
        }
        User updateUser = new User();
        updateUser.setEmail(email);
        updateUser.setEnable(1);
        userMapper.update(updateUser);
        // 让缓存失效
        registerCache.invalidate(key);
        return true;
    }


}
