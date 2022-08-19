package com.kimnit.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kimnit.reggie.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
