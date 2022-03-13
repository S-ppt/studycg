package com.changgou.oauth;

import com.alibaba.fastjson.JSON;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.jwt.crypto.sign.RsaSigner;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;

public class CreateJwtTestDemo {

    @Test
    public void createJwtTest() {
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

        Jwt jwt = JwtHelper.encode(JSON.toJSONString(playLoad), new RsaSigner(key));

        String encoded = jwt.getEncoded();
        System.out.println(encoded);


    }

    //解析令牌
    @Test
    public void parseJwt() {
        String token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhZGRyZXNzIjoiem5oIiwibmlrZW5hbWUiOiJ0b21jYXQiLCJhZ2UiOiIxNiJ9.A77TwqqN7TJu-mXJmr70-mDssUB9t7kSqfdeM3dMet8oa0XhoqgDFwSbcA3FsQ6tPxXtZuM9ue0oSiVvYfwDBBkNAPJQfA5PIPyXm8DBiUIrCrrgE3CSq7zgBHSWyBR6_CVA1we5gWxnAdPKfPtSoKERoTeih7r-C_Ew3F3p0eUBiZcO5cojUSZb1TmrBaCHkG-2MEdTs1ouQaBg2uJNH1SXsjVsJ8LebqaXf5_qeWDwwjSmcI2P7X33NbMrSKY1irnRQ31ux1R6BwpSOozqRw4PFtOgy72C2b3TX1iZmHqmNbvhaQnjTDD81iQZJht0PHs30D75KmWZPcmt2yz9YA";
        String pk = "-----BEGIN PUBLIC KEY-----MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkKXQ1c+aTV+BOmVoFAwxQzEL2mD2X8+/5kOS3l6mqZODq48I9dtxdNm1FVnReflkJYF/5fNZMY5nCXY2kO0KW12nyyQLHXh8757J5eb4mqRxR17wmcjCL3Uy1vjj9vaJ9Frg2cWjwCSKrzrPkYFv2xN+xPXj6ryGP5cci4/jhv846SD/+EXUTKVn3M1voBzFRAydFuFw44MgJKue6oPB1oWaLtEAuBpPn7eVg5Tyf5tXJBVelyFBgof55nOsKBAcntpGD4+HsecMABf8toUHptidYREuk4P8+5R04+De3v0oDkeUJQpauW9tu0jg8pzhHG3dj7SUpMS6q0AX7qKftwIDAQAB-----END PUBLIC KEY-----";
        Jwt jwt = JwtHelper.decodeAndVerify(token, new RsaVerifier(pk));

        String claims = jwt.getClaims();
        System.out.println(claims);
    }
}
