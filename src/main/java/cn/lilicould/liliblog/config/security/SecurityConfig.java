package cn.lilicould.liliblog.config.security;

import cn.lilicould.liliblog.filter.JwtAuthFilter;
import cn.lilicould.liliblog.filter.WebLogFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 安全配置类
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置跨域
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // 生产环境建议改成具体域名
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // 必须包含 Authorization
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置安全过滤器链
     *
     * @param http          HttpSecurity 对象，用于配置安全策略
     * @param jwtAuthFilter 自定义的JWT 认证过滤器，处理 Token 验证
     * @return SecurityFilterChain 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthFilter jwtAuthFilter,
                                           WebLogFilter webLogFilter
    ) {
        http
            // 禁用 CSRF 防护（前后端分离项目无需 CSRF Token）
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // 配置会话管理为无状态模式（适用于 JWT Token 认证，不创建服务端会话）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 配置请求授权规则
            .authorizeHttpRequests(auth -> auth
                    // 放行 Knife4j 相关静态资源
                    .requestMatchers(
                            "/doc.html",
                            "/nextdoc/**",   // NextDoc4j UI 的全部静态资源
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/swagger-config",
                            "/favicon.ico"
                    ).permitAll()

                    // 放行无需认证的路径
                    .requestMatchers("/auth/**").permitAll() // 认证相关接口（登录、注册等）
                    .requestMatchers(HttpMethod.GET, "/api/article/**").permitAll() // 放行文章接口的GET请求方法
                    .requestMatchers(HttpMethod.GET, "/api/category/**").permitAll() // 放行分类接口的GET请求方法
                    .requestMatchers(HttpMethod.GET, "/api/tag/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/user/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/comment/**").permitAll()

                    // 其他所有请求都需要认证才能访问
                    .anyRequest().authenticated()
            )
            .httpBasic(AbstractHttpConfigurer::disable) // 禁用HttpBasic认证：浏览器弹出用户名/密码对话框
            .formLogin(AbstractHttpConfigurer::disable); // 禁用表单认证

        // 将 JWT 过滤器添加到用户名密码过滤器之前
        // 确保请求先经过 Token 验证，再进行其他认证流程
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        http.addFilterBefore(webLogFilter, JwtAuthFilter.class); // 日志过滤器

        return http.build();
    }

    ///**
    // * Chain 1：OAuth2 登录链（有状态，需要 Session 临时保存授权码状态）
    // * 处理前端点击登录后的整个 OAuth2 流程
    // */
    //@Bean
    //@Order(2)
    //public SecurityFilterChain oauth2LoginChain(HttpSecurity http, SuccessHandler successHandler) throws Exception {
    //    http
    //            // 只匹配这些路径
    //            .securityMatcher("/auth/login/**", "/login/**", "/oauth2/**")
    //
    //            // 关闭 CSRF（OAuth2 登录流程由 Spring Security 自动处理 state 参数防 CSRF）
    //            .csrf(AbstractHttpConfigurer::disable)
    //
    //            // 需要 Session 来存 OAuth2 的 state 和 code（临时状态）
    //            .sessionManagement(session ->
    //                    session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
    //            )
    //
    //            .oauth2Login(oauth2 -> oauth2
    //                    // 自定义授权端点：前端访问 /auth/login/github 触发登录
    //                    .authorizationEndpoint(endpoint ->
    //                            endpoint.baseUri("/auth/login")
    //                    )
    //
    //                    // 登录成功处理器
    //                    .successHandler(successHandler)
    //
    //                    // 登录失败处理器
    //                    .failureHandler((request, response, exception) -> {
    //                        response.sendRedirect(  "http://localhost/login?error=" + exception.getMessage());
    //                    })
    //            );
    //
    //    return http.build();
    //}


    /**
     * 配置密码加密器
     * @return PasswordEncoder 密码加密器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
