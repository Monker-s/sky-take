package com.sky.service.impl;


import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishdto
     */
    @Override
    @Transactional      //由于要操作两张表，因此要添加事务保障一致性
    public void saveWithFlavor(DishDTO dishdto) {
        //向菜品表插入1条数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishdto,dish);
        dishMapper.insert(dish);
        //获取insert语句生产的主键值，（由于前端无法将dish_id传到后端，只能从insert方法中拿到
        //在xml文件中
        // useGeneratedKeys="true" keyProperty="id"）
        Long dishId = dish.getId();
        //向口味表插入n条数据
        List<DishFlavor> flavors = dishdto.getFlavors();

        if(flavors != null && flavors.size() > 0){
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            //插入n条数据
            dishFlavorMapper.insertBatch(flavors);  //批量插入
        }
    }
}
