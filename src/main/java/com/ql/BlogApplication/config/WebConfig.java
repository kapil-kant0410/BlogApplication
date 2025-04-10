package com.ql.BlogApplication.config;


import com.ql.BlogApplication.interceptor.AuthorInterceptor;
import com.ql.BlogApplication.interceptor.JwtAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    final JwtAuthInterceptor jwtAuthInterceptor;
    final AuthorInterceptor authorInterceptor;

    public WebConfig(JwtAuthInterceptor jwtAuthInterceptor,AuthorInterceptor authorInterceptor) {
        this.jwtAuthInterceptor = jwtAuthInterceptor;
        this.authorInterceptor=authorInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login", "/api/auth/register");

        registry.addInterceptor(authorInterceptor)
                .addPathPatterns("/api/post/create");

    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry resourceHandlerRegistry) {
         resourceHandlerRegistry.addResourceHandler("/uploads/**")
                 .addResourceLocations("file:uploads/");
    }


}
