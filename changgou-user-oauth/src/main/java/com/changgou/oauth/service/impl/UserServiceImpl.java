package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) throws UnsupportedEncodingException {

        //1.调用请求的地址
//        String url = "http://localhost:9200/oauth/token";
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        if (serviceInstance == null) {
            throw new RuntimeException("找不到对应的服务");
        }
        String url = serviceInstance.getUri() + "/oauth/token";
        /**
         * 请求的数据封装
         */
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap();
        multiValueMap.add("username", username);
        multiValueMap.add("password", password);
        multiValueMap.add("grant_type", "password");


        //请求头封装
        String Authorization = "Basic " + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()), "UTF-8");
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add("Authorization", Authorization);

        /**
         * 提交方式:
         * post提交
         * String var1 请求路径
         * HttpMethod var2 请求方式
         *
         * @Nullable HttpEntity<?> var3 请求数据
         * Class<T> var4 响应所转换的类型
         */
        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(multiValueMap, headerMap);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);


        //用户登录后的令牌信息
        Map<String, String> map = responseEntity.getBody();

        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token"));
        authToken.setRefreshToken(map.get("refresh_token"));
        authToken.setJti(map.get("jti"));


        return authToken;
    }
}
