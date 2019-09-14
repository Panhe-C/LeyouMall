package com.leyou.seckill.pojo;

import com.leyou.common.pojo.UserInfo;

public class SeckillMessage {

    private UserInfo userInfo;
    private SeckillGoods seckillGoods;

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public SeckillGoods getSeckillGoods() {
        return seckillGoods;
    }

    public void setSeckillGoods(SeckillGoods seckillGoods) {
        this.seckillGoods = seckillGoods;
    }

    public SeckillMessage(UserInfo userInfo, SeckillGoods seckillGoods) {
        this.userInfo = userInfo;
        this.seckillGoods = seckillGoods;
    }

    public SeckillMessage() {
    }
}
