package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;


@RestController
@RequestMapping("/user")
public class LoginController {

    //客户端ID
    @Value("${auth.clientId}")
    private String clientId;

    //秘钥
    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Autowired
    private UserService userService;

    @PostConstruct
    public void jj() {
        System.out.println(clientId + "---" + clientSecret);
    }

    @RequestMapping("/login")
    public Result login(String username, String password) throws UnsupportedEncodingException {
        if (StringUtils.isEmpty(username)) {
            throw new RuntimeException("用户名为空");
        }

        if (StringUtils.isEmpty(password)) {
            throw new RuntimeException("密码为空");
        }

        AuthToken authToken = userService.login(username, password, clientId, clientSecret);
        if (authToken != null) {

        return new Result(true, StatusCode.OK, "登录成功", authToken);

        }
        return new Result(false, StatusCode.ERROR, "登陆失败");
    }

}
