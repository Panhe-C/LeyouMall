package com.leyou.seckill.pojo;

import java.util.Date;

/**
 * 设置秒杀参数
 */
public class SeckillParameter {

    //要秒杀的sku id
    private Long id;

    //秒杀开始时间
    private Date startTime;
    //秒杀结束时间
    private Date endTime;

    //参与秒杀的商品数量
    private Integer count;
    //折扣
    private double discount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }
}
