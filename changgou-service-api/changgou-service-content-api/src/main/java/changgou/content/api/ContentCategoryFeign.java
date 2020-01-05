package changgou.content.api;


import changgou.content.pojo.ContentCategory;
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
@RequestMapping("/contentCategory")

public interface ContentCategoryFeign {


    /***
     * 根据ID查询ContentCategory数据
     * @param id
     * @return
     */
      @GetMapping("/{id}")
    public Result<ContentCategory> findById(@PathVariable("id") Long id);
    /***
     * 查询ContentCategory全部数据
     * @return
     */
     @GetMapping
    public Result<List<ContentCategory>> findAll();
}
