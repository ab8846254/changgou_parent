package com.changgou.controller;

import com.changgou.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Administrator
 */
@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 数据导入
     *
     * @return
     */
    @GetMapping("/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK, "导入数据成功");
    }

    /**
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam Map<String, String> searchMap) {
        Map map = skuService.queryMap(searchMap);

        return map;
    }


}
