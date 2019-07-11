package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**根据父节点的id查询子节点
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoriesById(@RequestParam(value = "pid",defaultValue = "0")Long pid){
        if(pid==null || pid.longValue()<0){
            //400:参数不合法
//            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
//            return new ResponseEntity<>(HttpStatus.BAD_GATEWAY);
            return ResponseEntity.badRequest().build();
        }
        List<Category> categories = this.categoryService.queryCategoriesByPid(pid);

        if(CollectionUtils.isEmpty(categories)){
            //404：资源服务器未找到
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            return ResponseEntity.notFound().build();
        }

        //200查询成功
        return ResponseEntity.ok(categories);

//        //500：服务器内部错误
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }



    @GetMapping
    public ResponseEntity<List<String>> queryNamesByIdS(@RequestParam("ids")List<Long> ids){
        List<String> names = this.categoryService.queryNameByIds(ids);

        if(CollectionUtils.isEmpty(names)){
            return ResponseEntity.notFound().build();
        }

        //200查询成功
        return ResponseEntity.ok(names);
    }


    /**
     * 根据3级分类id查询1-3级的分类
     * @param id
     * @return
     */
    @GetMapping("all/level")
    public ResponseEntity<List<Category>> queryAllByCid3(@RequestParam("id")Long id){
        List<Category> list = this.categoryService.quertAllByCid3(id);
        if(list == null || list.size() < 1){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(list);
    }

}
