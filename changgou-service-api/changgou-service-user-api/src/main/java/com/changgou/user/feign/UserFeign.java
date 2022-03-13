package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user")
@RequestMapping("/user")
public interface UserFeign {

    @GetMapping("/points/add")
    Result addPoints(@RequestParam Integer points);

    @GetMapping("/load/{id}")
    Result<User> findById(@PathVariable("id") String id);

}
