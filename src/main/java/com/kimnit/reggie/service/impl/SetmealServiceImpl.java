package com.kimnit.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimnit.reggie.common.CustomException;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.dto.SetmealDto;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.DishFlavor;
import com.kimnit.reggie.entity.Setmeal;
import com.kimnit.reggie.entity.SetmealDish;
import com.kimnit.reggie.mapper.SetMealMapper;
import com.kimnit.reggie.service.SetMealService;
import com.kimnit.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetmealDishService setmealDishService;

    //    新增套餐，同时保存套餐和菜品的关联关系
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息，表setmeal
        this.save (setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes ( );
        setmealDishes.stream ().map ((item) -> {
            item.setSetmealId (setmealDto.getId ());
            return item;
        }).collect (Collectors.toList ());

        //保存套餐和菜品的关联信息，表setmeal_dish
        setmealDishService.saveBatch (setmealDishes);

    }

    //删除套餐和菜品的关联数据
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
//        select count(*) from setmeal where id in (1,2,3) and status = 1
        //查询当前套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<> ();
        queryWrapper.in (Setmeal::getId,ids);
        queryWrapper.eq (Setmeal::getStatus,1);

        int count = this.count (queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0){
            throw  new CustomException ("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表的数据--setmeal
        this.removeByIds (ids);

        //delete * from setmeal_dish where setmeal_id in (1,2,3)
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        lambdaQueryWrapper .in (SetmealDish::getId,ids);

        //删除关系表中的数据--setmeal_dish
        setmealDishService.remove (lambdaQueryWrapper);

    }

    @Override
    public SetmealDto getByIdWithDish(Long ids) {
        //查询菜品基本信息,表setmeal
        Setmeal setmeal = this.getById (ids);

        SetmealDto setmealDto = new SetmealDto ();
        BeanUtils.copyProperties (setmeal,setmealDto);

        //查询套餐菜品信息,表setmeal_dish
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (ids != null,SetmealDish::getSetmealId,setmeal.getId ());

        List<SetmealDish> list = setmealDishService.list (queryWrapper);
        setmealDto.setSetmealDishes (list);

        return setmealDto;

    }

    @Override
    @Transactional
    public void updateWithdish(SetmealDto setmealDto) {
        //更新菜品表setmeal基本信息
        this.updateById (setmealDto);

        //清理当前菜品口味数据--setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (SetmealDish::getSetmealId,setmealDto.getId ());
        setmealDishService.remove (queryWrapper);

        //提交当前菜品口味数据--setmeal_dish表的update操作
        List<SetmealDish> flavors = setmealDto.getSetmealDishes ( );

        flavors.stream ().map ((item) -> {
            item.setSetmealId (setmealDto.getId ());
            return item;
        }).collect (Collectors.toList ());

        setmealDishService.saveBatch (flavors);
    }

    //根据ID查询图片
    @Override
    public String getImageById(Long id) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (Setmeal::getId,id);
        List<Setmeal> list = setMealService.list (queryWrapper);
        String image = "";
        for (Setmeal setmeal : list){
            image = setmeal.getImage ( );
        }

        return image;
    }
}
