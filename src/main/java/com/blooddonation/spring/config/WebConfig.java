package com.blooddonation.spring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Autowired
    private PortalAuthInterceptor portalAuthInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(portalAuthInterceptor)
                .addPathPatterns("/portal/**");
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login", "/admin/logout", "/css/**", "/js/**");
    }
}
Commit at 2026-08-27T09:28:16
Commit at 2026-08-16T04:44:50
Commit at 2026-08-04T10:17:35
Commit at 2026-09-15T13:21:43
Commit at 2026-08-08T19:01:37
Commit at 2026-08-23T07:40:37
Commit at 2026-08-06T20:15:36
Commit at 2026-08-04T03:30:23
Commit at 2026-08-16T23:49:26
Commit at 2026-08-31T01:18:15
Commit at 2026-08-10T18:31:13
Commit at 2026-08-29T03:35:22
Commit at 2026-08-25T01:12:12
Commit at 2026-09-16T19:12:49
Commit at 2026-08-18T01:22:25
Commit at 2026-08-28T04:46:08
Commit at 2026-08-05T00:13:47
Commit at 2026-08-16T16:28:09
Commit at 2026-09-05T03:19:43
Commit at 2026-08-13T08:30:31
Commit at 2026-08-10T22:12:36
