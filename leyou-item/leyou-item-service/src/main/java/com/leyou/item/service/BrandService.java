package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.util.StringUtil;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    private BrandMapper brandMapper;

    /**
     * 根据查询条件分页并排序查询品牌信息
     * @param key
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @return
     */
    public PageResult<Brand> queryBrandsByPage(String key, Integer page, Integer rows, String sortBy, Boolean desc) {

        //初始化example对象
        Example example = new Example(Brand.class);
        Example.Criteria criteria = example.createCriteria();


        //根据name模糊查询，或者根据首字母查询
        if(StringUtil.isNotEmpty(key)){
            criteria.andLike("name","%" + key + "%").orEqualTo("letter",key);
        }
        //添加分页条件
        PageHelper.startPage(page,rows);

        //添加排序条件
        if(StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy + " " + (desc?"desc":"asc"));
        }

        List<Brand> brands = this.brandMapper.selectByExample(example);

        //包装成pageInfo
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);

        //包装成分页结果集返回
        return new PageResult<>(pageInfo.getTotal(),pageInfo.getList());


    }

    /**
     * 新增品牌
     * @param brand
     * @param cids
     */
    @Transactional//添加事务
    public void saveBrand(Brand brand, List<Long> cids) {
        //形成两张表，brand，category_brand

        //先新增brand
//        boolean flag = this.brandMapper.insertSelective(brand) == 1;//相较与insert效率高些//加入事务注解之后就不需要判断了
        boolean flag = this.brandMapper.insertSelective(brand) == 1;//相较与insert效率高些

        //再新增中间表
//        if(flag){
            cids.forEach(cid -> {
                this.brandMapper.insertCategoryAndBrand(cid,brand.getId());//通用mapper只能操作单表
            });
//        }

    }

    public List<Brand> queryBrandsByCid(Long cid) {
        return this.brandMapper.selectBrandsByCid(cid);
    }
}
