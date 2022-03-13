package com.changgou.oauth.util;

import com.alibaba.fastjson.JSON;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class AdminToken {

    //管理员令牌发放
    public static String adminToken() {
        //加载证书  读取类路径中的文件
        ClassPathResource classPathResource = new ClassPathResource("changgou.jks");

        //读取证书数据,加载读取证书数据
        KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(classPathResource,"changgou".toCharArray());

        //获取证书中的一对密钥
        KeyPair keyPair = keyStoreKeyFactory.getKeyPair("changgou", "changgou".toCharArray());

        //获取私钥
        RSAPrivateKey key = (RSAPrivateKey) keyPair.getPrivate();

        //创建令牌,需要私钥加盐
        Map<String, Object> playLoad = new HashMap<>();
        playLoad.put("nikename", "tomcat");
        playLoad.put("address", "znh");
        playLoad.put("age", "16");
        playLoad.put("authorities", new String[]{"admin","oauth"});

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(playLoad), new RsaSigner(key));

        String encoded = jwt.getEncoded();
        return encoded;
    }
}
