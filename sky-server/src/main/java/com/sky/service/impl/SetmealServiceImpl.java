package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import net.bytebuddy.utility.nullability.AlwaysNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);

        //先将套餐的消息插入
        setmealMapper.insert(setmeal);
        //取出套餐中的菜品id,用于做关联关系
        Long setmealId = setmeal.getId();
        //将套餐中的菜品消息插入setmeal_dish表
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();
        if(setmealDishs != null && setmealDishs.size() > 0){
            setmealDishs.forEach(setmealdish ->{
                setmealdish.setSetmealId(setmealId);
            });
            //批量插入
            setmealDishMapper.insertBatch(setmealDishs);
        }
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 套餐起售和停售
     * @param status
     * @param id
     */
    @Override
    @AutoFill(value = OperationType.UPDATE)
    public void startOrStop(Integer status, Long id) {
        Setmeal setmeal = new Setmeal().builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }

    /**
     * 删除套餐
     * @param ids
     */
    @Override
    public void delete(List<Long> ids) {
        //查看是否有启售的
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //删除套餐
        setmealMapper.deleteByIds(ids);
        //删除套餐中的菜品
        setmealDishMapper.deleteBySetmealId(ids);

    }

    /**
     * 根据id查询套餐和套餐中的菜品
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        //先查询套餐
        Setmeal setmeal = setmealMapper.getById(id);
        //查菜品
        List<SetmealDish> setmealDish = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(setmealDish);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param setmealDTO
     */
    @Override
    public void update(SetmealDTO setmealDTO) {
        //更改套餐内容
        Setmeal setmeal = new Setmeal();   //原因：之前做的update 的参数是Setmeal
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        List<Long> ids = new ArrayList<>();
        ids.add(setmealDTO.getId());
       //修改菜品：删除套餐中的菜品，插入新的菜品
        setmealDishMapper.deleteBySetmealId(ids);
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes(); //获取当前修改的所有菜品信息
        if(setmealDishs != null && setmealDishs.size() > 0){
            setmealDishs.forEach(setmealdish ->{
                setmealdish.setSetmealId(setmealDTO.getId());
                //将当前套餐的id，赋值给每个菜品对象建立套餐和菜品的关联关系，为后面插入时做准备
            });
        }
        setmealDishMapper.insertBatch(setmealDishs);

    }

}
