package com.kimnit.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kimnit.reggie.entity.User;

public interface UserService extends IService<User> {
    //发送邮箱验证码
    public void sendMsg(String to,String subject,String context);
}
