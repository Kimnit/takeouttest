package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.entity.Orders;
import com.kimnit.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService ordersService;

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

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,Long number,String beginTime,String endTime){
        //构造分页构造器
        //条件过滤器
        //执行分页查询
        return null;
    }
}
