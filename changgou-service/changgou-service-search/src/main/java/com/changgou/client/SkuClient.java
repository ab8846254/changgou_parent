package com.changgou.client;

import com.changgou.goods.api.SkuFeign;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Administrator
 */
@FeignClient("goods")
public interface SkuClient extends SkuFeign {
}
