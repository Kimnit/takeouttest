package com.kimnit.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.dto.SetmealDto;
import com.kimnit.reggie.entity.Setmeal;

import java.util.List;

public interface SetMealService extends IService<Setmeal> {
    //    新增套餐，同时保存套餐和菜品的关联关系
    public void saveWithDish(SetmealDto setmealDto);

    //删除套餐和菜品的关联数据
    public void removeWithDish(List<Long> ids);

    //根据ID查询t套餐信息
    public SetmealDto getByIdWithDish(Long ids);

    //修改套餐，同时更新套餐对应的菜品，操作表：setmeal，setmeal_dish
    public void updateWithdish(SetmealDto setmealDto);

    //根据ID查询图片
    public String getImageById(Long setmealId);

}
