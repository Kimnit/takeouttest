package com.kimnit.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kimnit.reggie.entity.AddressBook;
import com.kimnit.reggie.mapper.AddressBookMapper;
import com.kimnit.reggie.service.AddressBookService;
import org.springframework.stereotype.Service;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements AddressBookService {
}
