package com.kimnit.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimnit.reggie.entity.Setmeal;
import com.kimnit.reggie.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SetmealDishMapper extends BaseMapper<SetmealDish> {
}
