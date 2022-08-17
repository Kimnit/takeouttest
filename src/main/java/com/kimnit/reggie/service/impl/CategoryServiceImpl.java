package com.kimnit.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimnit.reggie.common.CustomException;
import com.kimnit.reggie.entity.Category;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.Setmeal;
import com.kimnit.reggie.mapper.CategoryMapper;
import com.kimnit.reggie.service.CategoryService;
import com.kimnit.reggie.service.DishService;
import com.kimnit.reggie.service.SetMealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;

    /**
     * 进行判断，然后根据Id删除分类
     * @param id
     */
    @Override
    public void remove(Long id) {

        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<> ();
        //添加查询条件
        dishLambdaQueryWrapper.eq (Dish::getCategoryId,id);
        int dishcount = dishService.count (dishLambdaQueryWrapper);

        //如果当前分类是否关联菜品，如果关联，抛出异常
        if (dishcount > 0){
            //说明关联菜品
            throw new CustomException ("当前分类下关联了菜品，不能删除");
        }

        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<> ();
        //添加查询条件
        dishLambdaQueryWrapper.eq (Dish::getCategoryId,id);
        int setmealcount = dishService.count (dishLambdaQueryWrapper);

        //如果当前分类是否关联套餐，如果关联，抛出异常
        if (setmealcount > 0){
            //说明关联套餐
            throw new CustomException ("当前分类下关联了套餐，不能删除");
        }

        //正常删除分类
        super.removeById (id);
    }
}
