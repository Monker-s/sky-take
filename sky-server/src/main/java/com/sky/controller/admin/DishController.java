package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/admin/dish")
@Slf4j
@RestController
public class DishController {
    @Autowired
    private DishService dishService;
    /**
     * 新增菜品
     * @param dishdto
     * @return
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishdto){
        log.info("新增菜品:{}",dishdto);
        dishService.saveWithFlavor(dishdto);
        return Result.success();
    }
    /**
     * 菜品管理分页查询
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品管理分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除菜品
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品批量删除：{}",ids);
        dishService.delete(ids);
        return Result.success();
    }
}
