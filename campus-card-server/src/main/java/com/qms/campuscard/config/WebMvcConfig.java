package com.qms.campuscard.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 直接使用项目根目录下的upload目录
        String absoluteUploadPath = System.getProperty("user.dir") + File.separator + "upload";

        // 配置静态资源访问，同时支持/upload/**和/api/upload/**路径
        registry.addResourceHandler(urlPrefix + "/**", "/api" + urlPrefix + "/**")
                .addResourceLocations("file:" + absoluteUploadPath + File.separator);
    }
}
