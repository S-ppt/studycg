package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import com.changgou.goods.pojo.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

/****
 * @Author:shenkunlin
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {


    @Select("SELECT name,image FROM tb_brand where id in(select brand_id from tb_category_brand where category_id in(select id from tb_category WHERE `name`=#{categoryName}))")
    List<Map> findBrandListByCategoryName(@Param("categoryName")String categoryName);


    @Select("SELECT tb.* from tb_brand tb,tb_category_brand tcb WHERE tb.id=tcb.brand_id AND tcb.category_id=#{categoryid}")
    List<Brand> findByBrand(@Param("categoryid") Integer categoryid);
}
