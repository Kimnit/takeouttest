package com.kimnit.reggie.dto;

import com.kimnit.reggie.entity.Setmeal;
import com.kimnit.reggie.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("套餐菜品关系及分类名")
public class SetmealDto extends Setmeal {

    @ApiModelProperty("套餐菜品关系")
    private List<SetmealDish> setmealDishes;

    @ApiModelProperty("分类名")
    private String categoryName;
}
