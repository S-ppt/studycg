package com.changgou.goods.dao;

import com.changgou.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/****
 * @Author:shenkunlin
 * @Description:Spec的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SpecMapper extends Mapper<Spec> {

    @Select("SELECT name,options FROM tb_spec WHERE template_id in (SELECT template_id FROM tb_category where `name`=#{categoryName})")
    List<Map> findBrandListByCategoryName(@Param("categoryName") String categoryName);
}
