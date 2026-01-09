package com.cra.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    /**
     * 不需要登录校验的路径
     */
    private static final List<String> EXCLUDE_PATHS = new ArrayList<>();
    // TODO 生产环境需要删除这个类
    static {
        // 这里的路径将对所有微服务生效
        EXCLUDE_PATHS.add("/error");
        
        // 合同服务所有接口放行 (包含上传、下载、增删改查)
        // 注意：/api/contractsFile/** 包含了 /api/contractsFile/upload/**，所以不需要重复配置
        EXCLUDE_PATHS.add("/api/contracts/**");
        EXCLUDE_PATHS.add("/api/users/**");
        
        // 可以在这里添加更多全局放行的路径
        // EXCLUDE_PATHS.add("/api/user/login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器，校验规则为 StpUtil.checkLogin()
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                .addPathPatterns("/**")
                .excludePathPatterns(EXCLUDE_PATHS);
    }
}
