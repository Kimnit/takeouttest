package com.kimnit.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimnit.reggie.entity.DishFlavor;
import com.kimnit.reggie.mapper.DishFlavorMapper;
import com.kimnit.reggie.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
