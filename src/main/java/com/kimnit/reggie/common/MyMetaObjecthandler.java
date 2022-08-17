package com.kimnit.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据对象处理器
 */

@Component
@Slf4j
public class MyMetaObjecthandler implements MetaObjectHandler {
    /**
     * 插入操作，自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.info ("公共字段字段填充【insert】。。。");
        log.info (metaObject.toString ());
        metaObject.setValue ("createTime", LocalDateTime.now ());
        metaObject.setValue ("updateTime", LocalDateTime.now ());
        metaObject.setValue ("createUser", BaseConext.getThreadLocalId ());
        metaObject.setValue ("updateUser", BaseConext.getThreadLocalId ());
    }

    /**
     * 更新操作，自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {

        log.info ("公共字段字段填充【update】。。。");

        //获取线程ID
        long id = Thread.currentThread ().getId ();
        log.info ("线程id：{}",id);

        log.info (metaObject.toString ());
        metaObject.setValue ("updateTime", LocalDateTime.now ());
        metaObject.setValue ("updateUser", BaseConext.getThreadLocalId ());
    }
}
