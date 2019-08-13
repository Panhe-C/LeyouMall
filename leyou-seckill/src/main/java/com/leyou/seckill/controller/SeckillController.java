package com.leyou.seckill.controller;


import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.pojo.SeckillParameter;
import com.leyou.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;


/*
请求方式：POST
请求路径：api.leyou.com/api/seckill/addSeckill
参数：SeckillParameter对象
返回结果：200添加成功，500内部服务器异常
* */

@Controller
@RequestMapping
public class SeckillController {


    @Autowired
    private SeckillService seckillService;


    /**
     * 根据传入的商品id，将其设置为可以秒杀的商品
     * @param seckillParameter
     * @return
     */
    @PostMapping("addSeckill")
    public ResponseEntity<Boolean> addSeckillGoods(@RequestBody SeckillParameter seckillParameter){
        if(seckillParameter != null){
            this.seckillService.addSeckillGoods(seckillParameter);
        }else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok().build();
    }


    /**
     * 查询秒杀商品
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<SeckillGoods>> querySeckillGoods(){
        List<SeckillGoods> seckillGoods = this.seckillService.querySeckillGoods();
        if(CollectionUtils.isEmpty(seckillGoods)){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(seckillGoods);
    }


    @PostMapping("seck")
    public ResponseEntity<Long> seckillOrder(@RequestBody SeckillGoods seckillGoods){
        //1.创建订单
        Long id = this.seckillService.createOrders(seckillGoods);
        //2.判断秒杀是否成功（通过id是否创建来判断）
        if(id == null){
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
        }

        return ResponseEntity.ok(id);

    }

}
