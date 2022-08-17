package com.kimnit.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kimnit.reggie.common.R;
import com.kimnit.reggie.entity.Employee;
import com.kimnit.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpServerErrorException;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

//    员工登录
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1.将页面提交的数据进行MD5加密处理
        String password = employee.getPassword ( );
        password = DigestUtils.md5DigestAsHex (password.getBytes (  ));
        //2.根据用户名查数据库
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<> ( );
        queryWrapper.eq (Employee::getUsername, employee.getUsername ());
        Employee emp = employeeService.getOne (queryWrapper);
        //3.判断查询是否成功
        if(emp == null){
            return R.error ("登录失败");
        }
        //4.密码比对
        if(!emp.getPassword ().equals (password)){
            return R.error ("登录失败");
        }
        //5.查看状态是否可用
        if(emp.getStatus () == 0){
            return R.error ("账号已禁用");
        }
        //6.登录成功，将员工ID存入session并返回登录结果
        request.getSession ().setAttribute ("employee",emp.getId ());
        return R.success (emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清除session中保存的员工ID
        request.getSession ().removeAttribute ("employee");
        return R.success ("退出成功");
    }

    //新增员工
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee){
        log.info ("新增员工，员工信息为：{}",employee.toString ());

        //设置初始密码123456，进行MD5加密
        employee.setPassword (DigestUtils.md5DigestAsHex ("123456".getBytes (  )));

//        employee.setCreateTime (LocalDateTime.now ());
//        employee.setUpdateTime (LocalDateTime.now ());
//
//        //获得当前用户的ID
//        Long empId = (Long) request.getSession ().getAttribute ("employee");
//        employee.setCreateUser (empId);
//        employee.setUpdateUser (empId);

        employeeService.save (employee);

        return R.success ("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        log.info ("page = {},pageSize = {}, name = {}", page, pageSize, name);

        //分页构造器
        Page pageinfo = new Page ( page, pageSize );

        //条件构造器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper (  );
        //添加一个过滤条件
        queryWrapper.like (StringUtils.hasText (name), Employee::getName,name);
        //添加排序
        queryWrapper.orderByDesc (Employee::getUpdateTime);

        //执行查询
        employeeService.page (pageinfo, queryWrapper);

        return R.success (pageinfo);
    }

    /**
     * 根据ID修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Employee employee){

        log.info(employee.toString ());

        //获取线程ID
        long id = Thread.currentThread ().getId ();
        log.info ("线程id：{}",id);

//        Long empId = (Long) request.getSession ().getAttribute ("employee");
//        employee.setUpdateUser (empId);
//        employee.setUpdateTime (LocalDateTime.now ());
        employeeService.updateById (employee);
        return R.success ("员工信息修改成功");
    }

    /**
     * 根据ID查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee> getbyId(@PathVariable Long id){
        log.info ("根据id查询员工信息");
        Employee employee = employeeService.getById (id);
        if(employee != null){
            return R.success (employee);
        }
        return R.error ("没有查到对应员工信息");
    }
}
