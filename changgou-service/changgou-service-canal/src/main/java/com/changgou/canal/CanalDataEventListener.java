package com.changgou.canal;


import changgou.content.pojo.Content;
import com.alibaba.fastjson.JSON;

import com.alibaba.otter.canal.protocol.CanalEntry;

import com.changgou.client.ContentFeignClient;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.UpdateListenPoint;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;


import java.util.List;


/**
 * @author Administrator
 * 实现mysql数据监听
 */
@CanalEventListener
public class CanalDataEventListener {

    @Autowired
    private ContentFeignClient contentFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    /***
     * 监听新增数据
     * @param eventType
     * @param rowData
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        CanalEntry.Column afterColumns = rowData.getAfterColumns(1);
        redisOperation(afterColumns);

    }

    /***
     * 操作redis的工具类
     * @param afterColumns
     */
    public void redisOperation(CanalEntry.Column afterColumns) {
        String value = afterColumns.getValue();
        //把value转换成long类型的数据
        long contentId = Long.parseLong(value);
        //那这个ID通过远程调用去数据库查询出这条新增的数据
        List<Content> data = contentFeignClient.findByCategoryId(contentId).getData();
        //把对象转换成JSON格式
        String contentJson = JSON.toJSONString(data);
        //先去删除redis中的缓存，然后存入
        Boolean delete = redisTemplate.delete("content_" + contentId);
        //把数据存入redis中
        redisTemplate.boundValueOps("content_" + contentId).append(contentJson);
    }


    /***
     * 监听删除数据
     * @param eventType
     * @param rowData
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        CanalEntry.Column column = rowData.getBeforeColumnsList().get(1);
        redisOperation(column);
    }

    /***
     * 监听修改数据
     * @param eventType
     * @param rowData
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        CanalEntry.Column column = rowData.getBeforeColumnsList().get(1);
        redisOperation(column);
    }
}
