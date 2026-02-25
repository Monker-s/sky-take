package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询套餐id
     * @param dishId
     * @return
     */

    List<Long> getSetmealIdsByDishId(List<Long> dishId);

    /**
     * 批量插入套餐菜品数据
     * @param setmealDish
     */
    void insertBatch(List<SetmealDish> setmealDish);

    /**
     * 批量删除套餐菜品数据
     * @param ids
     */
    void deleteBySetmealId(List<Long> ids);

    /**
     * 根据套餐id查询套餐菜品数据
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);
}
