package com.leyou;

import com.leyou.item.pojo.Sku;
import com.leyou.seckill.client.GoodsClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class test {

    @Autowired
    private GoodsClient goodsClient;


    @Test
    public void testGoodsClient(){
        long skuId = 2868435;
        Sku sku = this.goodsClient.querySkuBySkuId(skuId);

        System.out.println(sku.getId());


    }
}
