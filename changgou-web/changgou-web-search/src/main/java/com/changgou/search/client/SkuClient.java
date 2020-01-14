package com.changgou.search.client;

import com.changgou.search.api.SkuFeign;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;


/**
 * @author Administrator
 */
@FeignClient("search")
@RequestMapping("/search")
public interface SkuClient extends SkuFeign {
}
