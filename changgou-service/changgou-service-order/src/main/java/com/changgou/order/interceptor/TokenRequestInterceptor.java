package com.changgou.order.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;

//@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {
    /**
     * feign调用之前执行,进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /**
         * 获取用户的信息
         * 将令牌封装到头文件中
         */
        //记录了当前用户请求的所有数据,包括请求头和请求参数等
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        //获取请求头中的数据
        Enumeration<String> headerNames = requestAttributes.getRequest().getHeaderNames();

        while (headerNames.hasMoreElements()) {
            //请求头中的key
            String headerKey = headerNames.nextElement();
            //获取请求头中的值
            String headerValue = requestAttributes.getRequest().getHeader(headerKey);
            System.out.println(headerValue + "=====>" + headerKey);

            //将请求头信息存放到头中,使用Feign调用的时候,会传递给下一个微服务
            requestTemplate.header(headerKey, headerValue);
        }

    }
}
