package com.sky.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Properties;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private Properties pageHelperProperties;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
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

    /**
     * 菜品查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 批量删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void delete(List<Long> ids) {
       //判断当前菜品是否能够删除，是否存在起售中的菜品
        for (Long id : ids) {
           Dish dish = dishMapper.getById(id);
           if(dish.getStatus()== StatusConstant.ENABLE){
               //当前菜品处于起售中，不能删除
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
           }
        }
       //判断是否被套餐关联了
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishId(ids);
        if (setmealIds != null && setmealIds.size() > 0){
            //当前菜品被套餐关联了
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除数据
        for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }

    }
}
