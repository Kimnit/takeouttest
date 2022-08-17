package com.kimnit.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.SetmealDish;

import java.util.List;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，操作表：Dish，dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据ID查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //修改菜品，同时更新菜品对应的口味数据，操作表：Dish，dish_flavor
    public void updateWithFlavor(DishDto dishDto);

    //修改菜品状态
    public void updateWithSetmealdish(Integer status,List<Long> ids);

    //套餐表中并没有本菜品且改菜品停售可删除
    public void removeWithDish(List<Long> ids);
}
