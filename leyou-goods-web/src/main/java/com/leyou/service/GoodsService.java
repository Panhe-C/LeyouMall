package com.leyou.service;


import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsCient;
import com.leyou.client.SpecificationClient;
import com.leyou.item.pojo.*;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class GoodsService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsCient goodsCient;
    @Autowired
    private SpecificationClient specificationClient;


    public Map<String,Object> loadData(Long spuId){
        Map<String,Object> model = new HashMap<>();

        //根据spuId查询spu
        Spu spu = this.goodsCient.querySpuById(spuId);
        //查询spuDetai
        SpuDetail spuDetail = this.goodsCient.querySpuDetailBySpuId(spuId);

        //查询分类：Map<String,Object>
        List<Map<String,Object>> categories = new ArrayList<>();
        List<Long> cids= Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(cids);
        for (int i = 0; i < cids.size(); i++) {
            Map<String,Object> category = new HashMap<>();
            category.put("id",cids.get(i));
            category.put("name",names.get(i));
            categories.add(category);
        }

        Brand brand = this.brandClient.queryBrandById(spu.getBrandId());

        List<Sku> skus = this.goodsCient.querySkusById(spuId);

        List<SpecGroup> groups = this.specificationClient.queryGroupsWithParam(spu.getCid3());

        //查询特殊的规格参数
        List<SpecParam> params = this.specificationClient.queryParams(null,spu.getCid3(),false,null);
        Map<Long,String> paramMap = new HashMap<>();
        params.forEach(specParam -> {
            paramMap.put(specParam.getId(),specParam.getName());
        });


        model.put("spu",spu);
        model.put("spuDetail",spuDetail);
        model.put("categories",categories);
        model.put("brand",brand);
        model.put("skus",skus);
        model.put("groups",groups);
        model.put("paramMap",paramMap);

        return model;

    }
}
