package com.leyou.search.listener;

import com.leyou.search.service.SearchService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoodsListener {

    @Autowired
    private SearchService searchService;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.SAVE.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",ignoreDeclarationExceptions = "true"),
            key = {"item.insert","item.update"}
    ))
    public void save(Long id) throws IOException {
        if(id == null){return;}
        this.searchService.save(id);//存入索引


//        try {
//            this.searchService.save(id);//存入索引
//        }catch (Exception e){
//            System.out.println("searchservice发生异常");
//        }
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "LEYOU.SEARCH.DELETE.QUEUE",durable = "true"),
            exchange = @Exchange(value = "LEYOU.ITEM.EXCHANGE",ignoreDeclarationExceptions = "true"),
            key = {"item.delete"}
    ))
    public void delete(Long id) throws IOException {
        if(id == null){return;}
        this.searchService.delete(id);
    }
}
