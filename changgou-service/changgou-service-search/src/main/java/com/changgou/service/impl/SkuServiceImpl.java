package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.client.SkuClient;
import com.changgou.dao.SkuEsMapper;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.service.SkuService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuClient skuClient;

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public void importData() {
        //调用查询查询所有商品数据
        Result<List<Sku>> skuResult = skuClient.findAll();
        List<SkuInfo> skuInfos = JSON.parseArray(JSON.toJSONString(skuResult.getData()), SkuInfo.class);

        for (SkuInfo skuInfo : skuInfos) {
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec(), Map.class);

            skuInfo.setSpecMap(specMap);
        }
        skuEsMapper.saveAll(skuInfos);

    }

    /**
     * 条件搜索
     *
     * @param queryMap
     * @return
     */
    @Override
    public Map<String, Object> queryMap(Map<String, String> queryMap) {
        //搜索条件构建
        NativeSearchQueryBuilder nativeSearchQueryBuilder = buildBasicQuery(queryMap);
        //集合搜索
        Map<String, Object> resultMap = searchList(nativeSearchQueryBuilder);

        Map<String, Object> groupMap = searchGroupList(nativeSearchQueryBuilder, queryMap);
        resultMap.putAll(groupMap);
        return resultMap;
    }

    /**
     * 分组数据查询->根据分类，品牌，规格
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Object> searchGroupList(NativeSearchQueryBuilder nativeSearchQueryBuilder, Map<String, String> queryMap) {
        //查询分类信息
        if (queryMap == null || StringUtils.isEmpty(queryMap.get("category"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        }
        if (queryMap == null || StringUtils.isEmpty(queryMap.get("brand"))) {
            nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuBrand").field("brandName"));
        }

        nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        //获取分组信息
        //定义一个Map存储所以分组数据
        Map<String, Object> groupMapResult = new HashMap<>();

        if (queryMap == null || StringUtils.isEmpty(queryMap.get("category"))) {
            StringTerms categoryTerms = aggregatedPage.getAggregations().get("skuCategory");
            //获取分类分组集合数据
            List<String> categoryList = getGroupList(categoryTerms);
            groupMapResult.put("categoryList", categoryList);
        }
        if (queryMap == null || StringUtils.isEmpty(queryMap.get("brand"))) {
            StringTerms brandTerms = aggregatedPage.getAggregations().get("skuBrand");
            //获取品牌分组集合数据
            List<String> brandList = getGroupList(brandTerms);
            groupMapResult.put("brandList", brandList);
        }
        StringTerms specTerms = aggregatedPage.getAggregations().get("skuSpec");
        //获取规格分组集合数据

        List<String> specList = getGroupList(specTerms);
        Map<String, Set<String>> specMap = putAllSpec(specList);
        groupMapResult.put("specMap", specMap);

        return groupMapResult;
    }

    /***
     *
     * 获取分组集合数据
     * @param stringTerms
     * @return
     */
    public List<String> getGroupList(StringTerms stringTerms) {
        List<String> GroupList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String feildName = bucket.getKeyAsString();
            GroupList.add(feildName);
        }
        return GroupList;
    }


    /**
     * 条件构建
     *
     * @param queryMap
     * @return
     */
    public NativeSearchQueryBuilder buildBasicQuery(Map<String, String> queryMap) {
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //加入搜索条件
        if (queryMap != null && queryMap.size() > 0) {
            String keywords = queryMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {
                boolQueryBuilder.must(QueryBuilders.queryStringQuery(keywords).field("name"));
            }
            //用户输入了分类
            if (!StringUtils.isEmpty(queryMap.get("category"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName", queryMap.get("category")));
            }
            //用户输入了品牌
            if (!StringUtils.isEmpty(queryMap.get("brand"))) {
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName", queryMap.get("brand")));
            }
            //规格过滤实现
            for (Map.Entry<String, String> entry : queryMap.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("spec_")) {
                    String value = entry.getValue();
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", value));
                }
            }
            String price = queryMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                String newPrice = price.replace("元", "").replace("以上", "");
                String[] prices = newPrice.split("-");
                if (prices != null && prices.length > 0) {
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(Integer.parseInt(prices[0])));
                    if (prices.length > 1) {
                        boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lt(Integer.parseInt(prices[1])));
                    }
                }
            }
        }
        //指定排序的域
        String sortField = queryMap.get("sortField");
        //指定排序的规则
        String sortRule = queryMap.get("sortRule");
        if (!StringUtils.isEmpty(sortField) && !StringUtils.isEmpty(sortRule)) {
            //如果以上条件都不为空(指定排序域，指定排序规则)
            nativeSearchQueryBuilder.withSort(new FieldSortBuilder(sortField).order(SortOrder.valueOf(sortRule)));
        }
        //用户如果不传分页参数默认第一页
        Integer pageNum = coverterPage(queryMap);
        Integer size = 30;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, size));
        //将boolQueryBuilder填充到nativeSearchQueryBuilder
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);
        return nativeSearchQueryBuilder;
    }

    /**
     * 接收前端传的参数
     *
     * @param queryMap
     * @return
     */
    public Integer coverterPage(Map<String, String> queryMap) {
        if (queryMap != null && queryMap.size() > 0) {
            try {
                int pageNum = Integer.parseInt(queryMap.get("pageNum"));
                return pageNum;
            } catch (Exception e) {
                //e.printStackTrace();
                return 1;
            }
        }
        return 1;
    }

    /**
     * 集合搜索
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Object> searchList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //高亮搜索
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        //前缀
        field.preTags("<em style=\"color:red;\">");
        //后缀
        field.postTags("</em>");
        //碎片长度:关键词数据的长度
        field.fragmentSize(100);

        nativeSearchQueryBuilder.withHighlightFields(field);
        // AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class);
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                //存储高亮数据
                List<T> list = new ArrayList<>();
                //获取所以数据
                for (SearchHit hit : response.getHits()) {
                    //分析结果集获取非高亮数据
                    SkuInfo skuInfo = JSON.parseObject(hit.getSourceAsString(), SkuInfo.class);
                    //获取高亮数据
                    HighlightField highlightField = hit.getHighlightFields().get("name");
                    if (highlightField != null && highlightField.getFragments() != null) {
                        Text[] fragments = highlightField.fragments();
                        StringBuffer stringBuffer = new StringBuffer();
                        for (Text fragment : fragments) {
                            stringBuffer.append(fragment.toString());
                        }
                        //非高亮数据指定的域替换成高亮数据
                        skuInfo.setName(stringBuffer.toString());
                    }
                    list.add((T) skuInfo);
                }
                return new AggregatedPageImpl<T>(list, pageable, response.getHits().totalHits);
            }
        });


        //封装的数据
        List<SkuInfo> content = page.getContent();
        //总条数
        long totalElements = page.getTotalElements();
        //总页数
        int totalPages = page.getTotalPages();
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        Pageable pageable = query.getPageable();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();

        //构建一个集合返回
        Map<String, Object> map = new HashMap<>();
        map.put("rows", content);
        map.put("total", totalElements);
        map.put("totalPages", totalPages);
        map.put("pageNumber",pageNumber);
        map.put("pageSize",pageSize);
        return map;
    }

    /**
     * 分类分组数据查询
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //查询分类信息
        NativeSearchQueryBuilder addAggregation = nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuCategory").field("categoryName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(addAggregation.build(), SkuInfo.class);
        //获取分组信息
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuCategory");
        List<String> categoryList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String categoryName = bucket.getKeyAsString();
            categoryList.add(categoryName);
        }
        return categoryList;
    }

    /**
     * 品牌分组数据查询
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    public List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //查询分类信息
        NativeSearchQueryBuilder addAggregation = nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("brandName").field("brandName"));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(addAggregation.build(), SkuInfo.class);
        //获取分组信息
        StringTerms stringTerms = aggregatedPage.getAggregations().get("brandName");
        List<String> brandList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String brandName = bucket.getKeyAsString();
            brandList.add(brandName);
        }
        return brandList;
    }


    /**
     * 规格分组数据查询
     *
     * @param nativeSearchQueryBuilder
     * @return
     */
    public Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //查询分类信息
        NativeSearchQueryBuilder addAggregation = nativeSearchQueryBuilder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(10000));
        AggregatedPage<SkuInfo> aggregatedPage = elasticsearchTemplate.queryForPage(addAggregation.build(), SkuInfo.class);
        //获取分组信息
        StringTerms stringTerms = aggregatedPage.getAggregations().get("skuSpec");
        List<String> specList = new ArrayList<>();
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String specName = bucket.getKeyAsString();
            specList.add(specName);
        }
        //规格汇总合并
        Map<String, Set<String>> allSpec = putAllSpec(specList);

        return allSpec;

    }

    /***
     * 规格汇总合并
     * @param specList
     * @return
     */
    public Map<String, Set<String>> putAllSpec(List<String> specList) {
        Map<String, Set<String>> allSpec = new HashMap<>();
        //1.循环speclist
        for (String spec : specList) {
            //2.将每个JSON字符串转成Map
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);

            //4.合并流程
            for (Map.Entry<String, String> entry : specMap.entrySet()) {

                //4.2取出当前Map并且获取对于的key 以及value
                String key = entry.getKey();
                String value = entry.getValue();
                //4.3将当前循环的数据合并到一个Map<String,Set<String>>
                //Set<String> specSet = new HashSet<>();
                Set<String> specSet = allSpec.get(key);
                if ((specSet == null)) {
                    specSet = new HashSet<>();

                }
                specSet.add(value);
                allSpec.put(key, specSet);
            }

        }
        return allSpec;
    }
}
