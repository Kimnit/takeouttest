package com.kimnit.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimnit.reggie.common.CustomException;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.DishFlavor;
import com.kimnit.reggie.entity.SetmealDish;
import com.kimnit.reggie.mapper.DishMapper;
import com.kimnit.reggie.service.DishFlavorService;
import com.kimnit.reggie.service.DishService;
import com.kimnit.reggie.service.SetmealDishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增菜品，同时插入菜品对应的口味数据
     * @param dishDto
     */
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到表dish
        this.save (dishDto);

        Long dishId = dishDto.getId ();//菜品ID

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors ( );
        flavors.stream ().map ((item) -> {
           item.setDishId (dishId);
           return item;
        }).collect (Collectors.toList ());

        //保存菜品的口味数据到表dish_flavor
        dishFlavorService.saveBatch (flavors);

    }

    /**
     * 根据ID查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息,表dish
        Dish dish = this.getById (id);

        DishDto dishDto = new DishDto ();
        BeanUtils.copyProperties (dish,dishDto);
        //查询菜品口味信息,表dish_flavor
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (DishFlavor::getDishId,dish.getId ());
        List<DishFlavor> flavors = dishFlavorService.list (queryWrapper);
        dishDto.setFlavors (flavors);

        return dishDto;
    }

    //修改菜品，同时更新菜品对应的口味数据，操作表：Dish，dish_flavor
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新菜品表dish基本信息
        this.updateById (dishDto);

        //清理当前菜品口味数据--dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (DishFlavor::getDishId,dishDto.getId ());
        dishFlavorService.remove (queryWrapper);

        //提交当前菜品口味数据--dish_flavor表的update操作
        List<DishFlavor> flavors = dishDto.getFlavors ( );

        flavors.stream ().map ((item) -> {
            item.setDishId (dishDto.getId ());
            return item;
        }).collect (Collectors.toList ());

        dishFlavorService.saveBatch (flavors);

        //更改套餐中菜品的价格
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        lambdaQueryWrapper.eq (SetmealDish::getDishId,dishDto.getId ());
        List<SetmealDish> list = setmealDishService.list (lambdaQueryWrapper);

        int count = setmealDishService.count (lambdaQueryWrapper);
        if(count > 0){
            for (SetmealDish setmealDish : list){
                setmealDish.setPrice (dishDto.getPrice ());
                setmealDishService.updateById (setmealDish);
            }
        }
    }

    //修改菜品状态
    @Override
    public void updateWithSetmealdish(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<> ( );
        queryWrapper.in (ids != null, Dish::getId, ids);

        List<Dish> list = dishService.list (queryWrapper);


        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        lambdaQueryWrapper.in (SetmealDish::getDishId,ids);

        int sdcount = setmealDishService.count ( lambdaQueryWrapper );
        if(sdcount > 0){
            throw new CustomException("该菜品在套餐中，不能修改状态");
        }else {
            for (Dish dish : list) {
                dish.setStatus (status);
                dishService.updateById (dish);
            }
        }
    }

    //套餐表中并没有本菜品且改菜品停售可删除
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询当前菜品状态，确定是否可以删除
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<> ();
        queryWrapper.in (Dish::getId,ids);
        queryWrapper.eq (Dish::getStatus,1);

        int count = this.count (queryWrapper);

        //如果不能删除，抛出一个业务异常
        if (count > 0){
            throw new CustomException ("菜品正在售卖中，不能删除");
        }

        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        lambdaQueryWrapper.in (SetmealDish::getDishId,ids);

        int sdcount = setmealDishService.count ( lambdaQueryWrapper );
        if(sdcount > 0){
            throw new CustomException ("该菜品在套餐中，不能删除");
        }

        //删除关系表中的数据--dish
        dishService.removeByIds (ids);
    }
}
