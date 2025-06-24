package zysy.iflytek.zhumang.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import zysy.iflytek.zhumang.common.interceptor.JwtInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.annotation.DbType;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Resource
    private JwtInterceptor jwtInterceptor;

    public WebConfig() {
        logger.debug("WebConfig initialized.");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.debug("Configuring JwtInterceptor for paths.");

        String[] includePatterns = {
                "/user/info",
                "/user/update",
                "/user/update/password",
                "/user/logout",
                "/user/manage/**",  // 用户管理相关接口
                "/policy",  // 创建政策 (POST /policy)
                "/policy/update/**", // 更新政策 (PUT /policy/update/{id})
                "/policy/delete/**", // 删除政策 (DELETE /policy/delete/{id})
                "/policy/favorite/**", // 收藏相关操作
                "/policy/favorites" // 获取用户收藏列表
        };
        String[] excludePatterns = {
                "/user/login/**",  // 登录相关
                "/user/register/**", // 注册相关
                "/user/userSig", // 新增的 UserSig 接口
                "/policy/page", // 政策分页查询
                "/policy/detail/**", // 获取政策详情 (GET /policy/detail/{id})
                "/swagger-resources/**", // Swagger相关
                "/webjars/**",
                "/v2/**",
                "/swagger-ui.html/**",
                "/doc.html/**"  // Knife4j文档
        };

        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns(includePatterns)  // 明确拦截所有需要认证的请求
                .excludePathPatterns(excludePatterns);

        logger.debug("JwtInterceptor include patterns: {}", (Object) includePatterns);
        logger.debug("JwtInterceptor exclude patterns: {}", (Object) excludePatterns);
        logger.debug("JwtInterceptor successfully added to registry.");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")  // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的请求方法
                .allowedHeaders("*")  // 允许所有请求头
                .allowCredentials(true)  // 允许携带cookie
                .maxAge(3600);  // 预检请求的有效期，单位为秒
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
} 