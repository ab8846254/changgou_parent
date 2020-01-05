package com.changgou.goods.test;

import com.changgou.goods.pojo.Sku;
import com.changgou.service.SkuService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsTest {

    @Autowired
    private SkuService skuService;
    @Test
    public void test(){
        List<Sku> all = skuService.findAll();
        for (Sku sku : all) {
            System.out.println(sku.getBrandName());
        }
    }
}
