package com.changgou.oauth.interceptor;

import com.changgou.oauth.util.AdminToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenRequestInterceptor implements RequestInterceptor {
    /**
     * feign调用之前执行,进行拦截
     * @param requestTemplate
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        /**
         * 从数据库加载用户信息
         * 1.没有令牌,Feign调用之前生成令牌(admin)
         * 2.Feign调用之前,令牌需要携带过去
         * 3.Feign调用之前,令牌需要放到头文件中
         * 4.请求:->Feign调用-->拦截器RequestInterceptor-->Feign调用之前进行拦截
         */

//        1.没有令牌,Feign调用之前生成令牌(admin)
        String adminToken = AdminToken.adminToken();
        requestTemplate.header("Authorization", "bearer " + adminToken);

    }
}
