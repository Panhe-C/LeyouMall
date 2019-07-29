package feign;


import com.leyou.LeyouGoodsWebApplication;
import com.leyou.client.GoodsClient;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.*;

@SpringBootTest(classes = LeyouGoodsWebApplication.class)
@RunWith(SpringRunner.class)
public class test {

    @Autowired
    private GoodsClient goodsClient;

    @Test
    public void test(){
        Long skuId = 2868435L;
        Sku sku = goodsClient.querySkuBySkuId(skuId);
        Spu spu = goodsClient.querySpuById(skuId);
        System.out.println(sku.getId());

    }
}
