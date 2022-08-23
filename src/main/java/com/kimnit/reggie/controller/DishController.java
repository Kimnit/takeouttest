package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.entity.*;
import com.kimnit.reggie.service.CategoryService;
import com.kimnit.reggie.service.DishFlavorService;
import com.kimnit.reggie.service.DishService;
import com.kimnit.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> save(@RequestBody DishDto dishDto){
        log.info (dishDto.toString ());

        dishService.saveWithFlavor (dishDto);

        return R.success ("新增菜品成功");
    }

    /**
     * 菜品信息分类查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("page")
    public R<Page> page(int page, int pageSize, String name){
//        构造分页构造器对象
        Page<Dish> pageInfo = new Page<> (page, pageSize);
        Page<DishDto> dishDtoPage = new Page<> ();

//        条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<> ();
        //添加过滤条件
        queryWrapper.like (name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc (Dish::getUpdateTime);

        //执行分页查询
        dishService.page (pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties (pageInfo,dishDtoPage,"records");

        List<Dish> records = pageInfo.getRecords ( );
        List<DishDto> list = records.stream ().map ((item) -> {
            DishDto dishDto = new DishDto ();

            BeanUtils.copyProperties (item,dishDto);

            Long categoryId = item.getCategoryId ( );//菜品分类ID
            //根据id查询分类对象
            Category category = categoryService.getById (categoryId);

            if (category != null){
                String categoryname = category.getName ();
                dishDto.setCategoryName (categoryname);
            }

            return dishDto;
        }).collect (Collectors.toList ());

        dishDtoPage.setRecords (list);

        return R.success (dishDtoPage);
    }

    /**
     * 根据ID查询菜品信息的对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public  R<DishDto> get(@PathVariable Long id){

        DishDto dishDto = dishService.getByIdWithFlavor (id);

        return R.success (dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> update(@RequestBody DishDto dishDto){
        log.info (dishDto.toString ());

        dishService.updateWithFlavor (dishDto);

        return R.success ("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    @GetMapping("/list")
    @Cacheable(value = "dishCache",key = "#dish.categoryId + '_' + #dish.status")
    public R<List<DishDto>> listR(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (dish.getCategoryId () != null,Dish::getCategoryId,dish.getCategoryId ());
        //查询起售菜品
        queryWrapper.eq (dish.getStatus () != null,Dish::getStatus,dish.getStatus ());

        queryWrapper.orderByAsc (Dish::getSort).orderByDesc (Dish::getUpdateTime);

        List<Dish> list = dishService.list (queryWrapper);

        List<DishDto> dtos = list.stream ().map ((item) -> {
            DishDto dishDto = new DishDto ();
            BeanUtils.copyProperties (item,dishDto);

            Long categoryId = item.getCategoryId ( );//菜品分类ID
            Long dishid = item.getId ( );//菜品ID

            //根据id查询分类对象
            Category category = categoryService.getById (categoryId);

            if (category != null){
                String categoryname = category.getName ();
                dishDto.setCategoryName (categoryname);
            }

            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
            lambdaQueryWrapper.eq (DishFlavor::getDishId,dishid);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavors = dishFlavorService.list (lambdaQueryWrapper);
            dishDto.setFlavors (dishFlavors);

            return dishDto;
        }).collect (Collectors.toList ());

        return R.success (dtos);
    }

    /**
     * 根据ID修改菜品信息
     * @param ,ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> update(@PathVariable Integer status,@RequestParam List<Long> ids) {
        dishService.updateWithSetmealdish (status,ids);

        return R.success ("修改菜品状态成功");
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info ("ids:{}",ids);

        dishService.removeWithDish (ids);

        return R.success ("删除套餐成功");
    }
}
