package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.kimnit.reggie.common.BaseConext;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.entity.AddressBook;
import com.kimnit.reggie.service.AddressBookService;
import com.sun.prism.impl.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地址簿管理
 */
@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId (BaseConext.getThreadLocalId ());
        log.info ("addressBook:{}",addressBook);
        addressBookService.save (addressBook);
        return R.success (addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public R<AddressBook> setDelfault(@RequestBody AddressBook addressBook){
        log.info ("addressBook:{}",addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<> ();
        wrapper.eq (AddressBook::getUserId,BaseConext.getThreadLocalId ());
        wrapper.set (AddressBook::getIsDefault,0);
        addressBookService.update (wrapper);

        addressBook.setIsDefault (1);
        addressBookService.updateById (addressBook);
        return R.success (addressBook);
    }

    /**
     * 根据ID查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById (id);
        if (addressBook != null){
            return R.success (addressBook);
        }else {
            return R.error ("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<> ();
        queryWrapper.eq (AddressBook::getUserId,BaseConext.getThreadLocalId ());
        queryWrapper.eq (AddressBook::getIsDefault,1);

        AddressBook addressBook = addressBookService.getOne (queryWrapper);

        if(addressBook == null){
            return R.error ("没有找到该对象");
        }else {
            return R.success (addressBook);
        }
    }

    /**
     * 查询指定用户的全部地址
     */
    @GetMapping("list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId (BaseConext.getThreadLocalId ());
        log.info ("addressBook:{}",addressBook);

        LambdaQueryWrapper<AddressBook> lambdaQueryWrapper = new LambdaQueryWrapper<> ();
        lambdaQueryWrapper.eq (null != addressBook.getUserId (),AddressBook::getUserId,addressBook.getUserId ());
        lambdaQueryWrapper.orderByDesc (AddressBook::getUpdateTime);

        return R.success (addressBookService.list (lambdaQueryWrapper));
    }

    /**
     * 根据ID修改地址信息
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){

        addressBook.setUpdateTime (LocalDateTime.now ());
        addressBookService.updateById (addressBook);

        return R.success ("修改地址成功");
    }

    /**
     * 根据地址id删除改地址信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam Long ids){

        addressBookService.removeById (ids);

        return null;
    }
}
