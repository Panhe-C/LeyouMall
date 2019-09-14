package com.leyou.elasticsearch;


import com.leyou.common.PageResult;
import com.leyou.item.bo.SpuBo;

import com.leyou.search.LeyouSearchApplication;
import com.leyou.search.client.GoodsCient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(classes = LeyouSearchApplication.class)
@RunWith(SpringRunner.class)
public class testElasticsearch {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SearchService searchService;
    @Autowired
    private GoodsCient goodsCient;


    @Test
    public void test(){
        //创建索引，会根据Goods类的@Document注解信息来创建
        this.elasticsearchTemplate.createIndex(Goods.class);
        //配置映射，会根据Goods类中的id、filed等字段来自动完成映射
        this.elasticsearchTemplate.putMapping(Goods.class);

        Integer page = 1;
        Integer rows = 100;

        do {
            PageResult<SpuBo> result = this.goodsCient.querySpuByPage(null,null,page,rows);
            List<SpuBo> items = result.getItems();
            List<Goods> goodsList = items.stream().map(spuBo -> {
                try {
                    return this.searchService.buildGoods(spuBo);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

            //执行数据新增
            this.goodsRepository.saveAll(goodsList);

            page++;
            rows=items.size();
        }while (rows == 100);
    }


}
