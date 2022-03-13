package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {


    @Autowired
    private CartService cartService;
    /**
     * 加入购物车
     * 1.需要商品id---以及商品个数
     */
    @GetMapping("/add")
    public Result add(Integer num, Long id) {
//        String username = "szitheima";
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        cartService.add(num, id, username);
        return new Result(true, StatusCode.OK, "购物车添加成功");
    }


    /**
     * 购物车列表
     */
    @GetMapping("/list")
    public Result<List<OrderItem>> list() {

        //用户的令牌信息-->解析令牌信息--->username
//        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
//        String tokenType = details.getTokenType();
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> list = cartService.list(username);
        return new Result(true, StatusCode.OK, "查询购物车成功", list);
    }

}
