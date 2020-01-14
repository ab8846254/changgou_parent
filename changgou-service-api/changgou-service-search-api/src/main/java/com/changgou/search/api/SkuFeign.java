package com.changgou.search.api;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Administrator
 */
@RequestMapping("/search")
public interface SkuFeign {
    /**
     *
     * @param searchMap
     * @return
     */
    @GetMapping
     Map search(@RequestParam Map<String,String> searchMap);
}
