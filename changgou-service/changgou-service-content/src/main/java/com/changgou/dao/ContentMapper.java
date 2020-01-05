package com.changgou.dao;
import changgou.content.pojo.Content;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Content的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface ContentMapper extends Mapper<Content> {
    /**
     * 根据分类ID查询所有分类信息
     * @param id
     * @return
     */
    @Select("select url,pic from tb_content where status=#{'1'} and category_id=#{id}  order by sort_order")
    List<Content> findByCategoryId(Long id);
}
