package com.changgou.search.controller;

import com.changgou.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.apache.ibatis.executor.statement.StatementUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuService skuService;


    @GetMapping
    public Map searchMap(@RequestParam(required = false) Map<String, String> searchMap) throws Exception {
        return skuService.search(searchMap);
    }

    @GetMapping("/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK, "import success");
    }
}
