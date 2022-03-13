package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    //数据存到那个redis中
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private SpuFeign spuFeign;

    @Override
    public void add(Integer num, Long id, String username) {
        //购物车商品移除
        if (num <= 0) {
            redisTemplate.boundHashOps("Cart_" + username).delete(id);
            Long size = redisTemplate.boundHashOps("Cart_" + username).size();
            if (size == null || size <= 0) {
                redisTemplate.delete("Cart_" + username);
            }

            return;
        }



        //1.调用feign查询商品
        Result<Sku> skuResult = skuFeign.findById(id);
        Sku sku = skuResult.getData();
        Result<Spu> spuResult = spuFeign.findById(sku.getSpuId());
        Spu spu = spuResult.getData();
        OrderItem orderItem = createOrderItem(num, id, sku, spu);


        //3.将需要添加的数据存入Redis中
        //第一个代表命名空间
        redisTemplate.boundHashOps("Cart_" + username).put(id, orderItem);
    }

    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> list = redisTemplate.boundHashOps("Cart_" + username).values();
        return list;
    }

    private OrderItem createOrderItem(Integer num, Long id, Sku sku, Spu spu) {
        //2.将要加入购物车的商品封装成OrderItem
        OrderItem orderItem = new OrderItem();
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(id);
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(num * orderItem.getPrice());
        orderItem.setImage(spu.getImage());
        return orderItem;
    }
}
