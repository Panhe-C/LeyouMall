package com.leyou.seckill.controller;


import com.leyou.common.pojo.UserInfo;
import com.leyou.seckill.interceptor.LoginInterceptor;
import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.pojo.SeckillMessage;
import com.leyou.seckill.pojo.SeckillParameter;
import com.leyou.seckill.service.SeckillService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/*
请求方式：POST
请求路径：api.leyou.com/api/seckill/addSeckill
参数：SeckillParameter对象
返回结果：200添加成功，500内部服务器异常
* */

@Controller
@RequestMapping
public class SeckillController implements InitializingBean {
    //Spirng的InitializingBean为bean提供了定义初始化方法的方式。InitializingBean是一个接口，它仅仅包含一个方法：afterPropertiesSet()。
    //在spring 初始化后，执行完所有属性设置方法(即setXxx)将自动调用 afterPropertiesSet(), 在配置文件中无须特别的配置， 但此方式增加了bean对spring 的依赖，应该尽量避免使用


    @Autowired
    private SeckillService seckillService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX = "leyou:seckill:stock";

    //减少redis的访问，设置一个标志位，当库存不足时直接返回即可，不用再访问redis
    private Map<Long, Boolean> localOverMap = new HashMap<>();


    /**
     * 根据传入的商品id，将其设置为可以秒杀的商品
     *
     * @param seckillParameter
     * @return
     */
    @PostMapping("addSeckill")
    public ResponseEntity<Boolean> addSeckillGoods(@RequestBody SeckillParameter seckillParameter) {
        if (seckillParameter != null) {
            this.seckillService.addSeckillGoods(seckillParameter);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }


    /**
     * 查询秒杀商品
     *
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<SeckillGoods>> querySeckillGoods() {
        List<SeckillGoods> seckillGoods = this.seckillService.querySeckillGoods();
        if (CollectionUtils.isEmpty(seckillGoods)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(seckillGoods);
    }


    @PostMapping("seck")
    public ResponseEntity<String> seckillOrder(@RequestBody SeckillGoods seckillGoods) {
        String result = "排队中";

        //内存标记，减少redis访问
        boolean over = localOverMap.get(seckillGoods.getSkuId());
        if(over){
            return ResponseEntity.ok(result);
        }

        //1.读取库存，减一后更新缓存
        BoundHashOperations<String, Object, Object> hashOperations = this.stringRedisTemplate.boundHashOps(KEY_PREFIX);
        String s = (String) hashOperations.get(seckillGoods.getSkuId().toString());
        if(s == null){
            return ResponseEntity.ok(result);
        }

        Long stock = hashOperations.increment(seckillGoods.getSkuId().toString(), -1);//秒杀库存减一

        //2.库存不足直接返回
        if (stock < 0) {
            localOverMap.put(seckillGoods.getSkuId(), true);
            return ResponseEntity.ok(result);
        }

        //3.更新库存
        hashOperations.delete(seckillGoods.getSkuId().toString());
        hashOperations.put(seckillGoods.getSkuId().toString(),String.valueOf(stock));


        //4.库存充足，请求入队
        //3.1获取用户信息
        UserInfo userInfo = LoginInterceptor.getLoginUser();
        SeckillMessage seckillMessage = new SeckillMessage(userInfo, seckillGoods);
        //3.2发送消息到队列
        this.seckillService.sendMessage(seckillMessage);
        return ResponseEntity.ok(result);


    }

    /**
     * 系统初始化，初始化秒杀商品数量
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //1.查询可以秒杀的商品
        List<SeckillGoods> seckillGoodss = this.seckillService.querySeckillGoods();
        if (seckillGoodss == null || seckillGoodss.size() == 0) {
            return;
        }

        BoundHashOperations<String, Object, Object> hashOperation = this.stringRedisTemplate.boundHashOps(KEY_PREFIX);
        if (hashOperation.hasKey(KEY_PREFIX)) {
            hashOperation.delete(KEY_PREFIX);
        }
        seckillGoodss.forEach(goods -> {
            hashOperation.put(goods.getSkuId().toString(), goods.getStock().toString());
            hashOperation.entries().forEach((m,n) -> localOverMap.put(Long.parseLong(m.toString()),false));
        });

        System.out.println("初始化完成");
    }
}
