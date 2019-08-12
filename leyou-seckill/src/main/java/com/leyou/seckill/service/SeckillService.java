package com.leyou.seckill.service;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Stock;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.seckill.client.GoodsClient;
import com.leyou.seckill.client.OrderClient;
import com.leyou.seckill.mapper.SeckillMapper;
import com.leyou.seckill.mapper.SkuMapper;
import com.leyou.seckill.mapper.StockMapper;
import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.pojo.SeckillParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class SeckillService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SeckillMapper seckillMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private OrderClient orderClient;
//
//    @Autowired
//    private GoodsService goodsService;

    /**
     * 添加秒杀商品
     * @param seckillParameter
     */
    @Transactional(rollbackFor = Exception.class)
    public void addSeckillGoods(SeckillParameter seckillParameter) {

        //为了方便测试，秒杀的开始和结束时间在系统内部设定。
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        seckillParameter.setStartTime(calendar.getTime());
        calendar.add(Calendar.HOUR,2);
        seckillParameter.setEndTime(calendar.getTime());

        //1.根据skuid查询商品
        Long skuId = seckillParameter.getId();
        Sku sku = this.goodsClient.querySkuBySkuId(skuId);
//        Sku sku = this.skuMapper.selectByPrimaryKey(skuId);
        //2.插入到秒杀商品列表中
        SeckillGoods seckillGoods = new SeckillGoods();
        seckillGoods.setTitle(sku.getTitle());
        seckillGoods.setStartTime(seckillParameter.getStartTime());
        seckillGoods.setEndTime(seckillParameter.getEndTime());
        seckillGoods.setEnable(true);
        seckillGoods.setSkuId(sku.getId());
        seckillGoods.setImage(sku.getImages());
        seckillGoods.setStock(seckillParameter.getCount());
        seckillGoods.setSeckillPrice((long) (sku.getPrice() * seckillParameter.getDiscount()));

        this.seckillMapper.insert(seckillGoods);

        //3.更新对应的库存信息，tb_stock
        Stock stock = stockMapper.selectByPrimaryKey(sku.getId());
        stock.setSeckillStock(stock.getSeckillStock() + seckillParameter.getCount());
        stock.setSeckillTotal(stock.getSeckillTotal() + seckillParameter.getCount());
        stock.setStock(stock.getStock() - seckillParameter.getCount());
        this.stockMapper.updateByPrimaryKeySelective(stock);

    }

    /**
     * 查询秒杀商品
     * @return
     */
    public List<SeckillGoods> querySeckillGoods() {
        Example example = new Example(SeckillGoods.class);
        example.createCriteria().andEqualTo("enable",true);

        List<SeckillGoods> seckillGoods = this.seckillMapper.selectByExample(example);

        return seckillGoods;
    }

    /**
     * 创建秒杀订单
     * 调用订单微服务创建订单，要封装order对象，根据order需要的数据进行填充
     * @param seckillGoods
     * @return
     */
    public Long createOrders(SeckillGoods seckillGoods) {
        Order order = new Order();
        order.setPaymentType(1);
        order.setTotalPay(seckillGoods.getSeckillPrice());
        order.setActualPay(seckillGoods.getSeckillPrice());
        order.setPostFee(0+"");
        order.setReceiver("李四");
        order.setReceiverMobile("15812312312");
        order.setReceiverCity("西安");
        order.setReceiverDistrict("碑林区");
        order.setReceiverState("陕西");
        order.setReceiverZip("000000000");
        order.setInvoiceType(0);
        order.setSourceType(2);



        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(seckillGoods.getSkuId());
        orderDetail.setNum(1);
        orderDetail.setTitle(seckillGoods.getTitle());
        orderDetail.setImage(seckillGoods.getImage());
        orderDetail.setPrice(seckillGoods.getSeckillPrice());
        orderDetail.setOwnSpec(this.skuMapper.selectByPrimaryKey(seckillGoods.getSkuId()).getOwnSpec());

        order.setOrderDetails(Arrays.asList(orderDetail));

        String seck = "seckill";
//        ResponseEntity<List<Long>> responseEntity = this.orderClient.createOrder(seck,order);
        ResponseEntity<Long> responseEntity = this.orderClient.createOrder(seck,order);

        if(responseEntity.getStatusCode() == HttpStatus.OK){
            //库存不足，返回null
            return null;
        }

        //修改秒杀商品的库存
        return responseEntity.getBody();
    }
}
