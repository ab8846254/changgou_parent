package com.changgou.service;

import java.util.Map;

/**
 * @author Administrator
 */
public interface SkuService {
    /***
     * 导入数据到索引库
     */
    void importData();

    /**
     * 条件搜索
     * @param queryMap
     * @return Map集合
     */
    Map<String,Object> queryMap(Map<String,String> queryMap);
}
