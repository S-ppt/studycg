package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuESMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SkuService;
import entity.Result;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class SkuServiceImpl implements SkuService {


    @Autowired
    private SkuESMapper skuESMapper;

    @Autowired
    private SkuFeign skuFeign;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map<String, Object> search(Map<String, String> searchMap) throws Exception {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(searchMap);


        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);
        //??????????????????

        /*if (searchMap.get("category") == null || StringUtils.isEmpty(searchMap.get("category"))) {
            List<String> categoryList = categoryList(nativeSearchQueryBuilder);
            resultMap.put("categoryList", categoryList);
        }


        if (searchMap.get("brand") == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
            resultMap.put("brandList", brandList);
        }


        if (searchMap.get("spec") == null || StringUtils.isEmpty(searchMap.get("spec"))) {
            Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);
            resultMap.put("specList", specList);
        }*/

        Map<String, Object> searchGroupList = searchGroupList(nativeSearchQueryBuilder, searchMap);
        resultMap.putAll(searchGroupList);
        return resultMap;
    }

    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));


        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);

        StringTerms skuSpec = skuInfos.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : skuSpec.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            specList.add(keyAsString);
        }

        Map<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;
    }

    private Map<String, Set<String>> putAllSpec(List<String> specList) {
        Map<String, Set<String>> allSpec = new HashMap<>();
        for (String str : specList) {
            Map<String, String> map = JSON.parseObject(str, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                String key = entry.getKey();
                Set<String> specSet = allSpec.get(key);
                if (specSet == null) {
                    specSet = new HashSet<>();
                }
                specSet.add(value);
                allSpec.put(key, specSet);
            }
        }
        return allSpec;
    }

    private List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrandName").field("brandName"));
        //????????????
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        StringTerms skuBrandName = skuInfos.getAggregations().get("skuBrandName");
        List<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : skuBrandName.getBuckets()) {
            String skuBrand = bucket.getKeyAsString();
            brandList.add(skuBrand);
        }
        return brandList;
    }

    /**
     * ??????????????????
     *
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder buildBasicQuery(Map<String, String> searchMap) {
        //1.??????????????????
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        //???????????????????????????
        if (searchMap != null && searchMap.size() > 0) {
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name")
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }

            if (!StringUtils.isEmpty(searchMap.get("category"))) {
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name")
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", searchMap.get("category")));
            }

            if (!StringUtils.isEmpty(searchMap.get("brand"))) {
                //nativeSearchQueryBuilder.withQuery(QueryBuilders.queryStringQuery(keywords).field("name")
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", searchMap.get("brand")));
            }

            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("spec_")) {
                    //??????????????????
                    String value = entry.getValue();
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                }
            }

            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                price = price.replace("???", "").replace("??????", "");
                String[] split = price.split("-");
                if (split != null && split.length > 0) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(split[0])));
                    if (split.length == 2) {
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(Integer.parseInt(split[1])));
                    }

                }
            }


        }

        String sortRule = searchMap.get("sortRule");
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
        }

        Integer pageNum = converPage(searchMap);
        Integer size = 10;

        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, size));
        //???boolquery???nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    public Integer converPage(Map<String, String> searchMap) {
        if (searchMap != null) {
            String pageNum = searchMap.get("pageNum");
            try {
                return Integer.parseInt(pageNum);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        return 1;
    }

    /**
     * ?????????????????????
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //????????????
        //???????????????????????????
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.preTags("<em style=\"color:red\">");
        field.postTags("</em>");
        field.fragmentSize(57);
        nativeSearchQueryBuilder.withHighlightFields(field);


        /**
         * ????????????,??????????????????
         * 1.??????????????????
         * 2.??????????????????(????????????)?????????????????????
         * 3.AggregatedPage<SkuInfo> skuInfos:????????????????????????
         */


        /***
         * ??????1:??????????????????
         * ??????2:??????????????????????????????????????????
         * ??????3:???????????????,????????????????????????????????????
         */
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {


            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                List<T> list = new ArrayList<>();

                //????????????,??????????????????,->?????????-->[???????????????|????????????]
                for (SearchHit hit : searchResponse.getHits()) {
                    //????????????????????????
                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);

                    //???????????????,??????????????????
                    HighlightField highlightField = hit.getHighlightFields().get("name");

                    //?????????????????????????????????
                    if (highlightField != null && highlightField.getFragments() != null) {
                        Text[] fragments = highlightField.getFragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text fragment : fragments) {
                            stringBuffer.append(fragment.toString());
                        }

                        skuInfo.setName(stringBuffer.toString());
                    }
                    list.add((T) skuInfo);
                }

                //???????????????
                /***
                 * 1.?????????????????????:(????????????)
                 * 2.??????????????????
                 * 3.?????????????????????
                 */

                return new AggregatedPageImpl<T>(list, pageable, searchResponse.getHits().getTotalHits());
            }
        });


        //????????????
        long totalElements = skuInfos.getTotalElements();

        //?????????
        int totalPages = skuInfos.getTotalPages();

        //??????
        List<SkuInfo> content = skuInfos.getContent();

        //????????????Map???????????????????????????
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("rows", content);
        resultMap.put("total", totalElements);
        resultMap.put("totalPages", totalPages);
        return resultMap;
    }

    /**
     * ??????????????????
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    private List<String> categoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));

        //????????????
        AggregatedPage<SkuInfo> skuInfosGroup = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        StringTerms skuCategory = skuInfosGroup.getAggregations().get("skuCategory");


        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : skuCategory.getBuckets()) {
            String categoryName = bucket.getKeyAsString();
            categoryList.add(categoryName);
        }
        return categoryList;
    }


    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String, String> searchMap) {
        if (searchMap.get("category") == null || StringUtils.isEmpty(searchMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (searchMap.get("brand") == null || StringUtils.isEmpty(searchMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));

        Map<String, Object> groupList = new HashMap<>();

        //????????????
        AggregatedPage<SkuInfo> skuInfosGroup = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        if (searchMap != null || StringUtils.isEmpty(searchMap.get("category"))) {
            StringTerms categoryTerms = skuInfosGroup.getAggregations().get("skuCategory");
            List<String> categoryList = getGroupList(categoryTerms);
            groupList.put("categoryList", categoryList);
        }
        if (searchMap != null || StringUtils.isEmpty(searchMap.get("brand"))) {
            StringTerms brandTerms = skuInfosGroup.getAggregations().get("skuBrand");
            List<String> brandList = getGroupList(brandTerms);
            groupList.put("brandList", brandList);
        }
        StringTerms specTerms = skuInfosGroup.getAggregations().get("skuSpec");
        List<String> specList = getGroupList(specTerms);

        groupList.put("specList", specList);
        Map<String, Set<String>> setMap = putAllSpec(specList);
        groupList.put("specList", setMap);
        return groupList;
    }

    private List<String> getGroupList(StringTerms stringTerms) {
        List<String> stringTermsList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String termsName = bucket.getKeyAsString();
            stringTermsList.add(termsName);
        }
        return stringTermsList;
    }

    @Override
    public void importData() {
        Result<List<Sku>> result = skuFeign.findAll();

        List<SkuInfo> skuInfo = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);


        //
        for (SkuInfo info : skuInfo) {
            Map<String, Object> map = JSON.parseObject(info.getSpec(), Map.class);

            info.setSpecMap(map);
        }

        skuESMapper.saveAll(skuInfo);
    }
}
