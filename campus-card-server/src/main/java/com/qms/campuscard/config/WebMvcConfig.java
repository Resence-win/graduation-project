package com.qms.campuscard.config;

import com.qms.campuscard.util.UploadPathResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;
import java.nio.file.Path;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private UploadPathResolver uploadPathResolver;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path absoluteUploadPath = uploadPathResolver.resolveRootPath();

        // 配置静态资源访问，同时支持/upload/**和/api/upload/**路径
        registry.addResourceHandler(urlPrefix + "/**", "/api" + urlPrefix + "/**")
                .addResourceLocations(absoluteUploadPath.toUri().toString());
    }
}
