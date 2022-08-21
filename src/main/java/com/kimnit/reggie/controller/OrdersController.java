package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimnit.reggie.common.BaseConext;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.dto.OrdersDto;
import com.kimnit.reggie.entity.Dish;
import com.kimnit.reggie.entity.OrderDetail;
import com.kimnit.reggie.entity.Orders;
import com.kimnit.reggie.entity.ShoppingCart;
import com.kimnit.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private SetMealService setMealService;

    @Autowired
    private DishService dishService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info ("订单数据：{}",orders);
        ordersService.submit (orders);
        return R.success ("下单成功");
    }

    /**
     * 后台订单管理
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,Long number,String beginTime,String endTime){
        //构造分页构造器
        Page<Orders> ordersPage = new Page<> (page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (number != null,Orders::getNumber,number)
                .gt (beginTime != null,Orders::getOrderTime,beginTime)
                .lt (endTime != null,Orders::getOrderTime,endTime);

        //执行分页查询
        ordersService.page (ordersPage,queryWrapper);

        return R.success (ordersPage);
    }

    /**
     * 订单状态修改
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> delivery(@RequestBody Orders orders){
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (Orders::getId,orders.getId ());

        ordersService.updateById (orders);

        return R.success ("修改派送状态成功");
    }

    /**
     * 用户查询自己的订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        //构造分页构造器
        Page<Orders> ordersPage = new Page<> (page,pageSize);
        Page<OrdersDto> pageDto = new Page<> (page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (Orders::getUserId,BaseConext.getThreadLocalId ());
        queryWrapper.orderByDesc (Orders::getOrderTime);

        //执行分页查询
        ordersService.page (ordersPage,queryWrapper);

        LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        List<Orders> records = ordersPage.getRecords ();
        List<OrdersDto> list = records.stream ().map ((item) -> {
            OrdersDto ordersDto = new OrdersDto ();

            BeanUtils.copyProperties (item,ordersDto);

            Long orderId = item.getId ( );
            LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
            orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);

            ordersDto.setOrderDetails (orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        pageDto.setRecords (list);

        return R.success (pageDto);
    }

    /**
     * 再来一单
     * @param orders
     * @return
     */
    @PostMapping("again")
    public R<String> again(@RequestBody Orders orders){
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (OrderDetail::getOrderId,orders.getId ());

        List<OrderDetail> list = orderDetailService.list (queryWrapper);
        List<ShoppingCart> cartList = list.stream ().map ((item) -> {
            ShoppingCart shoppingCart = new ShoppingCart ();

            Long currentId = BaseConext.getThreadLocalId ();
            shoppingCart.setUserId (currentId);

            BeanUtils.copyProperties (item,shoppingCart);

            return shoppingCart;
        }).collect(Collectors.toList());

        if (cartList != null){
            for (ShoppingCart shoppingCartOrder : cartList){
                String image = null;
                if (shoppingCartOrder.getDishId () != null){
                    image = dishService.getImageById (shoppingCartOrder.getDishId ());
                }else {
                    image = setMealService.getImageById (shoppingCartOrder.getSetmealId ());
                }

                shoppingCartOrder.setCreateTime (LocalDateTime.now ());
                shoppingCartOrder.setImage (image);
                shoppingCartService.save (shoppingCartOrder);
            }
            return R.success ("添加至购物车成功");
        }else {
            return R.error ("添加至购物车失败");
        }

    }
}
