package com.calcifer.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 将所有非 API 的路径重定向到 index.html，交给前端路由处理
     * 解决 React 路由刷新 404 问题
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 排除 /api/** 和 /ws-tunnel/** 等后端路径
        // 这里简单处理：如果路径不包含点（即不是静态资源文件）且不是 api 开头，就跳到 index.html
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{path:[^\\.]*}/**")
                .setViewName("forward:/index.html");
    }
}
