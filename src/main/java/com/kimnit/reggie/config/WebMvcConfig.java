package com.kimnit.reggie.config;

import com.kimnit.reggie.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

@Slf4j
@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info ("开始进行静态资源映射...");
        registry.addResourceHandler ("/backend/**").addResourceLocations ("classpath:/backend/");
        registry.addResourceHandler ("/front/**").addResourceLocations ("classpath:/front/");
    }

    /**
     * 拓展mvc框架的消息装换器
     * @param converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //创建消息装换器
        MappingJackson2HttpMessageConverter  mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter ();
        //设置对象装换器，底层使用Jackson将java转为json
        mappingJackson2HttpMessageConverter.setObjectMapper (new JacksonObjectMapper ());
        //将上面的创建消息装换器对象追加到MVC框架的转换器集合中
        converters.add (0,mappingJackson2HttpMessageConverter);
    }
}
