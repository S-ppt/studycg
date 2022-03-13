package com.changgou.goods.feign;


import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    @GetMapping("/desc/count")
    Result descCount(@RequestParam Map<String, Integer> descmap);

    @GetMapping
    Result<List<Sku>> findAll();


    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable Long id);
}
