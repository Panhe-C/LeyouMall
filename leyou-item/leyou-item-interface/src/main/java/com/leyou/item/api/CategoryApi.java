package com.leyou.item.api;


import com.leyou.item.pojo.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping("category")
public interface CategoryApi {
    @GetMapping
    public List<String> queryNamesByIds(@RequestParam("ids")List<Long> ids);


    /**根据父节点的id查询子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public List<Category> queryCategoriesById(@RequestParam(value = "pid",defaultValue = "0")Long pid);
}
