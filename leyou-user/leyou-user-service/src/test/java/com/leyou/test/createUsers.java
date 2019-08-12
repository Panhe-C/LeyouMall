package com.leyou.test;


import com.leyou.user.LeyouUserApplication;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.utils.CodecUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = LeyouUserApplication.class)
public class createUsers {

    @Autowired
    private UserMapper userMapper;


    /**
     * 注册5000个用户
     */
    @Test
    public void addUser(){
        User user = new User();
        for (int i = 1; i < 5000; i++) {
            user.setId(null);
            user.setCreated(new Date());
            user.setPhone("1820010" + String.format("%04d",i));

            user.setUsername("user_no." + i);
            user.setPassword("1234" + i);

//            String encodePassword = CodecUtils.p

            //2.生成盐
            String salt = CodecUtils.generateSalt();
            user.setSalt(salt);

            //3.加盐加密
            user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));

            //4.新增用户
            this.userMapper.insertSelective(user);
        }
    }


}
