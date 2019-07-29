package com.leyou.user.service;

import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String USER_PREFIX = "user:verify:";

    /**
     * 校验数据是否可用(是否与已经存在)
     * 1：校验用户名
     * 2：校验手机号
     * @param data
     * @param type
     * @return
     */
    public Boolean checkUser(String data, Integer type) {

        User record = new User();
        if(type == 1){
            record.setUsername(data);
        }else if(type == 2){
            record.setPhone(data);
        }else{
            return null;
        }

        return this.userMapper.selectCount(record) == 0;
    }

    public void sendVerifyCode(String phone) {

        if(StringUtils.isBlank(phone)){
            return;
        }

        //生成验证码
        String code = NumberUtils.generateCode(6);

        //发送消息到rabbitMq
        Map<String,String> msg = new HashMap<>();
        msg.put("phone",phone);
        msg.put("code",code);
        this.amqpTemplate.convertAndSend("leyou.sms.exchange","verifycode.sms",msg);

        //把验证码保存到redis
        this.redisTemplate.opsForValue().set(USER_PREFIX+phone,code,5, TimeUnit.MINUTES);
    }

    public void register(User user, String code) {

        //0.查询redis的验证码
        String redisCode = this.redisTemplate.opsForValue().get(USER_PREFIX + user.getPhone());

        //1.校验验证码
        if(!StringUtils.equals(code,redisCode)){
            return;
        }

        //2.生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);

        //3.加盐加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

        //4.新增用户
        user.setId(null);
        user.setCreated(new Date());
        this.userMapper.insertSelective(user);


        //5.从redis删除使用过了的code

        this.redisTemplate.delete(USER_PREFIX + user.getPhone());
    }

    public User queryUser(String username, String password) {
        User record = new User();
        record.setUsername(username);

        //先通过username查询用户，获取用户其他信息
        User user = this.userMapper.selectOne(record);
        if(user == null){
            return null;
        }

        //得到用户的盐值，对获得的密码进行加盐加密
        String salt = user.getSalt();

        password = CodecUtils.md5Hex(password, salt);

        //把对用户输入的密码加盐加密后再与数据库中的密码进行比对
        String pwd = user.getPassword();
        if(StringUtils.equals(pwd,password)){
            return user;
        }

        return null;


    }
}
