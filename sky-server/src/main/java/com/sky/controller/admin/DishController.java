package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.result.Result;
import com.sky.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
