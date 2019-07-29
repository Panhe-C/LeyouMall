package com.leyou.item.api;

import com.leyou.common.PageResult;
import com.leyou.item.bo.SpuBo;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("goods")
public interface GoodsApi {


    /**
     * 根据条件分页查询spu
     * @param key
     * @param saleable
     * @param page
     * @param rows
     * @return
     */
    @GetMapping("spu/page")
    public PageResult<SpuBo> querySpuByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows

    );
   /**
     * 根据spuId查询spuDetail
     * @param spuId
     * @return
     */
    @GetMapping("spu/detail/{spuId}")
    public SpuDetail querySpuDetailBySpuId(@PathVariable("spuId")Long spuId);

    /**
     * 根据spuId查询Sku集合
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    public List<Sku> querySkusById(@RequestParam("id")Long spuId);


    @GetMapping("{id}")
    public Spu querySpuById(@PathVariable("id")Long id);


    /**
     * 根据skuId查询sku
     * @param skuId
     * @return
     */
    @GetMapping("id/{skuId}")
    public Sku querySkuBySkuId(@PathVariable("skuId")Long skuId);

    }
