package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.dto.DishDto;
import com.kimnit.reggie.dto.SetmealDto;
import com.kimnit.reggie.entity.Category;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.Employee;
import com.kimnit.reggie.entity.Setmeal;
import com.kimnit.reggie.service.CategoryService;
import com.kimnit.reggie.service.SetMealService;
import com.kimnit.reggie.service.SetmealDishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealContriller {

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info ("套餐信息：{}",setmealDto);

        setMealService.saveWithDish (setmealDto);

        return R.success ("新增套餐成功");
    }

    /**
     * 套餐分类查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page<Setmeal> pageInfo = new Page<> ( page,pageSize );
        Page<SetmealDto> dtoPage = new Page<> (  );

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<> ();
        //添加查询条件，根据Like模糊查询
        queryWrapper.like (name != null,Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc (Setmeal::getUpdateTime);

        setMealService.page (pageInfo,queryWrapper);

        BeanUtils.copyProperties (pageInfo,dtoPage,"records");

        List<Setmeal> records = pageInfo.getRecords ();
        List<SetmealDto> list = records.stream ().map ((item) -> {
            SetmealDto setmealDto = new SetmealDto ();
            //对象拷贝
            BeanUtils.copyProperties (item,setmealDto);

            Long categoryId = item.getCategoryId ();
            //根据分类ID查询对象
            Category category = categoryService.getById (categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName ();
                setmealDto.setCategoryName (categoryName);
            }
            return  setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords (list);

        return R.success (dtoPage);
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param ids
     * @return
     */
    @GetMapping("/{ids}")
    public R<SetmealDto> listR(@PathVariable Long ids){

        SetmealDto setmealDto = setMealService.getByIdWithDish (ids);

        return R.success (setmealDto);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        log.info (setmealDto.toString ());

        setMealService.updateWithdish (setmealDto);

        return R.success ("修改套餐成功");
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info ("ids:{}",ids);

        setMealService.removeWithDish (ids);

        return R.success ("删除套餐成功");
    }

    /**
     * 根据ID修改套餐信息
     * @param ,ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> update(@PathVariable Integer status,@RequestParam List<Long> ids){

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.in (ids != null, Setmeal::getId,ids);

        List<Setmeal> list = setMealService.list (queryWrapper);
        if (list != null){
            for (Setmeal setmeal : list) {
                setmeal.setStatus(status);
                setMealService.updateById(setmeal);
            }
            return R.success("套餐状态修改成功！");
        }

        return R.error("套餐状态不能修改,请联系管理或客服！");
    }
}