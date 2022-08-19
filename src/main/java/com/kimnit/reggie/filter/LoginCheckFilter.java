package com.kimnit.reggie.filter;

//检查用户是否完成登录

import com.alibaba.fastjson.JSON;
import com.kimnit.reggie.common.BaseConext;
import com.kimnit.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "LoginCheckFilter", urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    //路径匹配器，支持通配符
    public static final AntPathMatcher PATH_MATCHER = new AntPathMatcher ();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //1.获取本次的URI
        String requestURI = request.getRequestURI ( );
        log.info ("拦截到请求：{}",requestURI);

        //定义不需要处理的请求路径
        String[] urls = new String[]{
                "/employee/login",
                "/employee/loginout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/**"
        };

        //2.判断本次请求是否需要处理
        boolean check = check (requestURI,urls);

        //3.如果不需要处理。直接放行
        if(check == true){
            log.info ("本次{}请求不需要处理",requestURI);
            filterChain.doFilter (request,response);
            return;
        }

        //4-1.判断登录状态，如果已登录，则直接放行
        if (request.getSession ().getAttribute ("employee") != null){

            log.info ("用户已登录，用户ID为：{}",request.getSession ().getAttribute ("employee"));

            //获取当前用户ID
            Long empId = (Long) request.getSession ( ).getAttribute ("employee");
            BaseConext.setThreadLocalId ( empId );

            //获取线程ID
            long id = Thread.currentThread ().getId ();
            log.info ("线程id：{}",id);

            filterChain.doFilter (request,response);
            return;
        }

        //4-2.判断移动端登录状态，如果已登录，则直接放行
        if (request.getSession ().getAttribute ("user") != null){

            log.info ("用户已登录，用户ID为：{}",request.getSession ().getAttribute ("user"));

            //获取当前用户ID
            Long userId = (Long) request.getSession ( ).getAttribute ("user");
            BaseConext.setThreadLocalId ( userId );

            //放行
            filterChain.doFilter (request,response);
            return;
        }

        log.info ("用户未登录");
        //5.如果为登录则返回未登录结果
        response.getWriter ().write (JSON.toJSONString (R.error ("NOTLOGIN")));

        return;
    }

    //路径匹配，判断本次是否需要放行
    public boolean check(String requestURI,String[] urls){
        for(String url : urls){
            boolean match = PATH_MATCHER.match (url, requestURI);
            if(match == true){
                return true;
            }
        }
        return false;
    }
}
