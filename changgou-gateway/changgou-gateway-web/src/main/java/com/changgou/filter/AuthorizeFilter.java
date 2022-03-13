package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {

    //令牌的名字
    private static final String AUTHORIZE_TOKEN = "Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //1.获取request,response对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();


        //用户如果是登录或者一些不需要做权限认证的请求,直接放行
        String uri = request.getURI().toString();
        if (!URLFilter.hasAuthorize(uri)) {
            return chain.filter(exchange);
        }

        //获取令牌信息
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        boolean flag = true;
        //参数获取令牌
        if (StringUtils.isEmpty(token)) {
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            flag = false;
        }

        //3.cookie中获取
        if (StringUtils.isEmpty(token)) {
            HttpCookie httpCookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (httpCookie != null) {
                token = httpCookie.getValue();
            }
        }

        //没有令牌拦截
        if (StringUtils.isEmpty(token)) {
            //设置响应吗
            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            //响应空数据
            return response.setComplete();
        }

        //如果有令牌校验令牌是否有效--->老写法
        /*try {
//            JwtUtil.parseJWT(token);//对接微服务网关之后,这里就需要使用了


        } catch (Exception e) {
            //无效拦截
            //设置响应吗
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }*/

        ////判断令牌是否为空,如果不为空的话,将令牌放到头文件中 放行
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        } else {
            if (!flag) {
                //            判断当前token是否以Bearer开头
                if (!token.startsWith("bearer ") && !token.startsWith("Bearer ")) {
                    token = "bearer " + token;
                }
                //将令牌放入到请求头中
                request.mutate().header(AUTHORIZE_TOKEN, token);
            }

        }

        //有效放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
