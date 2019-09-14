package leyou.order.listener;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.Stock;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.pojo.SeckillOrder;
import com.leyou.seckill.pojo.SeckillGoods;
import com.leyou.seckill.pojo.SeckillMessage;
import leyou.order.mapper.*;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class SeckillListener {

    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 接收秒杀消息
     * @param seck
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.ORDER.SECKILL.QUEUE",durable = "true"),//队列持久化
            exchange = @Exchange(
                    value = "LEYOU.ORDER.EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC
            ),
            key = {"order.seckill"}
    ))
    public void listenSeckill(String seck){
        System.out.println("从队列中收到秒杀的消息");

        //消息解析
        SeckillMessage seckillMessage = JsonUtils.parse(seck,SeckillMessage.class);
        UserInfo userInfo = seckillMessage.getUserInfo();
        SeckillGoods seckillGoods = seckillMessage.getSeckillGoods();

        //库存判断
        Stock stock = this.stockMapper.selectByPrimaryKey(seckillGoods.getSkuId());
        if(stock.getSeckillStock() <= 0 || stock.getStock() <= 0){
            return;
        }

        //2.判断用户是否已经秒杀到了
        Example example = new Example(SeckillOrder.class);
        example.createCriteria().andEqualTo("userId",userInfo.getId()).andEqualTo("skuId",seckillGoods.getSkuId());
        List<SeckillOrder> seckillOrders = this.seckillOrderMapper.selectByExample(example);
        if(seckillOrders.size() > 0){//若已经秒杀到了，则返回
            return;
        }


        //3.下订单
        //构造order对象
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

        //3.1生成orderId
        long orderId = idWorker.nextId();
        //3.2初始化数据
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);
        order.setCreateTime(new Date());
        order.setOrderId(orderId);
        order.setUserId(userInfo.getId());
        //3.3 保存数据
        this.orderMapper.insertSelective(order);

        //3.4保存订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());

        //订单初始未付款状态
        orderStatus.setStatus(1);
        //3.5保存数据
        this.orderStatusMapper.insertSelective(orderStatus);

        //3.6在订单详情中添加orderId
        order.getOrderDetails().forEach(orderDetail1 -> {
            orderDetail1.setOrderId(orderId);
        });

        //3.7保存订单详情，使用批量插入
        this.orderDetailMapper.insertList(order.getOrderDetails());

        //3.8修改库存
        order.getOrderDetails().forEach(orderDeta ->{
            Stock stock1 = this.stockMapper.selectByPrimaryKey(orderDeta.getSkuId());
            stock1.setStock(stock1.getStock() - orderDeta.getNum());
            stock1.setSeckillStock(stock1.getStock() - orderDeta.getNum());
            this.stockMapper.updateByPrimaryKeySelective(stock1);

            //新建秒杀订单
            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setOrderId(orderId);
            seckillOrder.setSkuId(orderDeta.getSkuId());
            seckillOrder.setUserId(userInfo.getId());
            this.seckillOrderMapper.insertSelective(seckillOrder);
        });








    }
}
