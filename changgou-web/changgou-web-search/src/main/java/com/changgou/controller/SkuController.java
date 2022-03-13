package com.changgou.controller;

import com.changgou.search.feign.SkuFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {


    @Autowired
    private SkuFeign skuFeign;

    //实现页面跳转
    @GetMapping("/list")
    public String search(@RequestParam(required = false)Map<String, String> searchMap, Model model) throws Exception {

        Map<String, Object> map = skuFeign.searchMap(searchMap);

        String url = url(searchMap);

        model.addAttribute("result", map);
        //搜索添加封装
        model.addAttribute("searchMap", searchMap);
        return "search";
    }

    //拼接组装用户请求的url路径
    public String url(Map<String, String> searchMap) {
        String url = "/search/list";

        if (searchMap != null && searchMap.size() > 0) {
            url += "?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //key是搜索的条件对象
                String key = entry.getKey();
                //value:搜索的值
                String value = entry.getValue();
                url += "key" + "=" + value + "&";
            }
        }
        //去掉最后一个&
        url = url.substring(0, url.length() - 1);

        return url;
    }
}
