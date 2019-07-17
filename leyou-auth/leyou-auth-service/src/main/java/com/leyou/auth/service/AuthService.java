package com.leyou.auth.service;


import com.leyou.auth.client.AuthClient;
import com.leyou.auth.config.JwtProperties;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private AuthClient authClient;

    @Autowired
    private JwtProperties jwtProperties;

    public String accredit(String username,String password){



        //1.根据用户名和密码查询
        User user = authClient.queryUser(username, password);

        //2.判断user
        if(user == null){return null;}

        //3.jwtUtils生成jwt类型的token
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());

        try {
            String token = JwtUtils.generateToken(userInfo, this.jwtProperties.getPrivateKey(), this.jwtProperties.getExpire());
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}
