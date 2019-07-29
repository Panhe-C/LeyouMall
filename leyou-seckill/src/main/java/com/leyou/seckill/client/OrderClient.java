package com.leyou.seckill.client;

import com.leyou.order.service.api.OrderApi;
import com.leyou.seckill.config.OrderConfig;
import org.springframework.cloud.openfeign.FeignClient;

/*
*订单微服务在创建订单的时候要进行用户登录认证，
* 所以这里面通过feign client来调用订单服务的时候需要将header中的信息进行转发。因为订单微服务需要拿到token。
*  */


@FeignClient(value = "order-service",configuration = OrderConfig.class)
public interface OrderClient extends OrderApi {
}
