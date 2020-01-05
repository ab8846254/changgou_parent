package com.changgou.controller;


import changgou.content.pojo.Content;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.changgou.service.ContentService;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/14 0:18
 *****/
@Api(value = "ContentController")
@RestController
@RequestMapping("/content")
@CrossOrigin
public class ContentController {

    @Autowired
    private ContentService contentService;

    /***
     * Content分页条件搜索实现
     * @param content
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false) @ApiParam(name = "Content对象",value = "传入JSON数据",required = false) Content content, @PathVariable  int page, @PathVariable  int size){
        //调用ContentService实现分页条件查询Content
        PageInfo<Content> pageInfo = contentService.findPage(content, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Content分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
  @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用ContentService实现分页查询Content
        PageInfo<Content> pageInfo = contentService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param content
     * @return
     */
     @PostMapping(value = "/search" )
    public Result<List<Content>> findList(@RequestBody Content content){
        //调用ContentService实现条件查询Content
        List<Content> list = contentService.findList(content);
        return new Result<List<Content>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        //调用ContentService实现根据主键删除
        contentService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Content数据
     * @param content
     * @param id
     * @return
     */
   @PutMapping(value="/{id}")
    public Result update(@RequestBody  Content content,@PathVariable Long id){
        //设置主键值
        content.setId(id);
        //调用ContentService实现修改Content
        contentService.update(content);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增Content数据
     * @param content
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Content content){
        //调用ContentService实现添加Content
        contentService.add(content);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询Content数据
     * @param id
     * @return
     */
   @GetMapping("/{id}")
    public Result<Content> findById(@PathVariable("id") Long id){
        //调用ContentService实现根据主键查询Content
        Content content = contentService.findById(id);
        return new Result<Content>(true,StatusCode.OK,"查询成功",content);
    }

    /***
     * 查询Content全部数据
     * @return
     */
      @GetMapping
    public Result<List<Content>> findAll(){
        //调用ContentService实现查询所有Content
        List<Content> list = contentService.findAll();
        return new Result<List<Content>>(true, StatusCode.OK,"查询成功",list) ;
    }

    /**
     * 根据分类ID查询所有广告数据
     * @param id
     * @return
     */
    @GetMapping("/findByCategoryId/{id}")
    public Result<List<Content>> findByCategoryId(@PathVariable Long id){
        List<Content> byCategoryId = contentService.findByCategoryId(id);
        return new Result<List<Content>>(true, StatusCode.OK,"查询成功",byCategoryId) ;
    }
}
