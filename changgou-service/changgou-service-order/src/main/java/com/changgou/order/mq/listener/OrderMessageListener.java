package com.changgou.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Component
@RabbitListener(queues = "${mq.pay.queue.order}")
public class OrderMessageListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void getMessage(String message) throws ParseException {
        JSONObject jsonObject = JSON.parseObject(message);

        Map<String, String[]> map = JSONObject.parseObject(jsonObject.toJSONString(), new TypeReference<Map<String, String[]>>() {
        });
        //        Map<String, String[]> map = JSON.parseObject(message, Map.class);
        System.out.println("监听到的消息");
        //对结果进行判断
        String trade_status = map.get("trade_status")[0];
        //支付成功修改订单状态

        if (trade_status.equals("TRADE_SUCCESS")) {
            //支付宝交易号
            String trade_no = map.get("trade_no")[0];
            String out_trade_no = map.get("out_trade_no")[0];
            String gmt_payment = map.get("gmt_payment")[0];

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date parse = simpleDateFormat.parse(gmt_payment);

            orderService.updateStatus(out_trade_no, parse, trade_no);
        } else {
            //支付失败,关闭支付,取消订单,回滚库存
            String out_trade_no = map.get("out_trade_no")[0];
            orderService.deleteOrder(out_trade_no);
        }
    }

}
