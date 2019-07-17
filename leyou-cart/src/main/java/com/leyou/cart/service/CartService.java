package com.leyou.cart.service;


import com.leyou.cart.client.CartClient;
import com.leyou.cart.interceptor.LoginInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Sku;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private StringRedisTemplate redisTemplatel;
    @Autowired
    private CartClient cartClient;

    private static final String KEY_PREFIX = "user:cart:";



    public void addCart(Cart cart) {

        //获得登录用户信息
        UserInfo userInfo = loginInterceptor.getUserInfo();

        //设置redis的key
        String key = KEY_PREFIX + userInfo.getId();

        //查询购物车信息
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplatel.boundHashOps(key);

        //查询是否存在
        Long skuId = cart.getSkuId();
        Integer num = cart.getNum();
        if(hashOps.hasKey(skuId.toString())){
            //若商品存在购物车中，则更新数量

            //获取购物车数据
            String jsonCart = hashOps.get(skuId.toString()).toString();
            cart = JsonUtils.parse(jsonCart,Cart.class);
            //更新数量
            cart.setNum(cart.getNum() + num);
        }else{
            //若商品不在购物车中，则新添订单
            cart.setUserId(userInfo.getId());

            Sku sku = cartClient.querySkuBySkuId(skuId);
            cart.setTitle(sku.getTitle());
            cart.setOwnSpec(sku.getOwnSpec());
            cart.setImage(StringUtils.isBlank(sku.getImages()) ? "" : StringUtils.split(sku.getImages(),",")[0]);
            cart.setPrice(sku.getPrice());
        }

        //将购物车数据写入redis
        hashOps.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));


    }

    /**
     * 查询所有购物车信息
     * @return
     */
    public List<Cart> queryCarts() {
        //获得登录用户信息
        UserInfo userInfo = loginInterceptor.getUserInfo();

        String key = KEY_PREFIX + userInfo.getId();

        //若不存在购物车数据，直接返回
        if(!this.redisTemplatel.hasKey(key)){return null;}

        //查询购物车信息
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplatel.boundHashOps(key);
        List<Object> cartsJson = hashOps.values();

        return cartsJson.stream().map(cartJson->JsonUtils.parse(cartJson.toString(),Cart.class)).collect(Collectors.toList());

    }

    /**
     * 更新购物车的商品的数量
     * @param cart
     */
    public void updayeCarts(Cart cart) {
        //获得登录用户信息
        UserInfo userInfo = loginInterceptor.getUserInfo();

        String key = KEY_PREFIX + userInfo.getId();
        String skuId = cart.getSkuId().toString();
        int num = cart.getNum();
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplatel.boundHashOps(KEY_PREFIX+userInfo.getId());



        //获取购物车数据
        String jsonCart = hashOps.get(skuId).toString();
        cart = JsonUtils.parse(jsonCart,Cart.class);
        //更新数量
        cart.setNum(num);

        hashOps.put(skuId,JsonUtils.serialize(cart));
    }

    public void deleteCart(Long skuId) {
        //获取登录用户
        UserInfo userInfo = loginInterceptor.getUserInfo();

        String key = KEY_PREFIX + userInfo.getId();


        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplatel.boundHashOps(key);
        hashOps.delete(skuId);
    }
}
