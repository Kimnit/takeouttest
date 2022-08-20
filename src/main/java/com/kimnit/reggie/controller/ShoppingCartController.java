package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kimnit.reggie.common.BaseConext;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.entity.ShoppingCart;
import com.kimnit.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info ("购物车数据:{}",shoppingCart);

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseConext.getThreadLocalId ();
        shoppingCart.setUserId (currentId);

        //查询当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId ( );

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (ShoppingCart::getUserId,currentId);

        if(dishId != null){
            //添加到购物车的是菜品
            queryWrapper.eq (ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper.eq (ShoppingCart::getSetmealId,shoppingCart.getSetmealId ());
        }
        //SQL:select * from shopping_cart where user_id = ? and dish(setmeal)_id = ?
        ShoppingCart cartServiceOne = shoppingCartService.getOne (queryWrapper);

        if (cartServiceOne != null){
            //当前菜品或套餐在购物车中，number+1
            Integer cartNumber = cartServiceOne .getNumber ( );
            log.info ("数量",cartNumber);
            cartServiceOne.setNumber (cartNumber + 1);
            shoppingCartService.updateById (cartServiceOne);
        }else {
            //当前菜品或套餐不在购物车中，则添加到购物车中
            shoppingCart.setNumber (1);
            shoppingCart.setCreateTime (LocalDateTime.now ());
            shoppingCartService.save (shoppingCart);
            cartServiceOne = shoppingCart;
        }

        return R.success (cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> listR(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (ShoppingCart::getUserId,BaseConext.getThreadLocalId ());
        queryWrapper.orderByDesc (ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list (queryWrapper);

        return R.success (list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> delete(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (ShoppingCart::getUserId,BaseConext.getThreadLocalId ());

        shoppingCartService.remove (queryWrapper);

        return R.success ("清空购物车成功");
    }
}
