package com.changgou.search.controller;

import com.changgou.search.client.SkuClient;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * @author Administrator
 */
@Controller
@RequestMapping("/search")
public class SkuController {

   @Autowired
    private SkuClient skuClient;

    @GetMapping("/list")
    public String search(@RequestParam Map<String, String> searchMap, Model model) {
        Map<String,Object> resultMap = skuClient.search(searchMap);
        long total = Long.parseLong(resultMap.get("total").toString());
        int pageNumber = Integer.parseInt(resultMap.get("pageNumber").toString() + 1);

        int pageSize = Integer.parseInt(resultMap.get("pageSize").toString());
        //计算分页
        Page<SkuInfo> pageInfo = new Page<>(total,pageNumber,pageSize);


        model.addAttribute("pageInfo",pageInfo);
        model.addAttribute("result",resultMap);
        model.addAttribute("searchMap",searchMap);
        String[] URLs=url(searchMap);
        model.addAttribute("url",URLs[0]);
        model.addAttribute("sortURL",URLs[1]);
        return "search";

    }
    /**
     * 组装搜索条件
     *
     * @param searchMap
     * @return
     */
    public String[] url(Map<String, String> searchMap) {
        String URL = "/search/list";
        String sortURL = "/search/list";
        if (searchMap != null && searchMap.size() > 0){
            URL+="?";
            sortURL+="?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                URL+=key+"="+value+"&";
                sortURL+=key+"="+value+"&";
            }
            URL=URL.substring(0,URL.length()-1);
            sortURL=sortURL.substring(0,sortURL.length()-1);
        }
        return new String[]{URL,sortURL};
    }
}
