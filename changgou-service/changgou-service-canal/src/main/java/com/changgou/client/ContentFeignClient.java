package com.changgou.client;
import changgou.content.api.ContentFeign;
import org.springframework.cloud.openfeign.FeignClient;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient("content")
public interface ContentFeignClient extends ContentFeign {

}