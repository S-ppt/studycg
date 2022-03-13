package com.changgou.search.service;

import java.util.Map;

public interface SkuService {


    Map<String, Object> search(Map<String, String> searchMap) throws Exception;

    /**
     *
     */
    void importData();
}
