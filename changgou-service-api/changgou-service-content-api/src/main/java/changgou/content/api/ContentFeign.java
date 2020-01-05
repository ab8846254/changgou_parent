package changgou.content.api;


import changgou.content.pojo.Content;
import entity.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RequestMapping("/content")
public interface ContentFeign {

    /**
     * 根据分类ID查询所有广告数据
     * @param id
     * @return
     */
    @GetMapping("/findByCategoryId/{id}")
    public Result<List<Content>> findByCategoryId(@PathVariable Long id);

    /***
     * 根据ID查询Content数据
     * @param id
     * @return
     */
   @GetMapping("/{id}")
    public Result<Content> findById(@PathVariable("id") Long id);

    /***
     * 查询Content全部数据
     * @return
     */
      @GetMapping
    public Result<List<Content>> findAll();
}
