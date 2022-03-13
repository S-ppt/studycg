package com.changgou;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {

    @Test
    public void testCreateJwt() {
        JwtBuilder builder = Jwts.builder().setId("123")
                .setSubject("哈哈")
                .setIssuedAt(new Date())
                .setExpiration(new Date())
                .signWith(SignatureAlgorithm.HS256, "hahfs");

        //自定义信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("address", "北京");
        userInfo.put("age", 18);
        userInfo.put("sex", "famale");

        builder.addClaims(userInfo);

        System.out.println(builder.compact());
    }
    @Test
    public void testParseJwt() {
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMjMiLCJzdWIiOiLlk4jlk4giLCJpYXQiOjE2NDY5MjI1NzEsImV4cCI6MTY0NjkyMjU3MX0.1yV-KqeoMF3ei1VVFvMevP-nA2yiFTZyX6NB58K2QxY";
        Claims claims = Jwts.parser().setSigningKey("hahfs")
                .parseClaimsJws(jwt).getBody();

        System.out.println(claims);
    }
}
