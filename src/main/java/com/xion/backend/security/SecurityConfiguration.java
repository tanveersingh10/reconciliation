package com.xion.backend.security;


import com.xion.backend.security.properties.SecurityProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String LOGIN_PROCESSING_URL = "/login";
    private static final String LOGIN_FAILURE_URL = "/login?error";
    private static final String LOGIN_URL = "/login";
    private static final String LOGOUT_SUCCESS_URL = "/login";

    @Autowired
    private UserDetailsService customUserDetailsService;

    @Autowired
    private FirebaseFilter tokenAuthenticationFilter;

    @Autowired
    private SecurityProperties restSecProps;

//    @Bean
//    public AuthenticationEntryPoint restAuthenticationEntryPoint() {
//        return (httpServletRequest, httpServletResponse, e) -> {
//            ObjectMapper mapper = new ObjectMapper();
//            Map<String, Object> errorObject = new HashMap<>();
//            int errorCode = 401;
//            errorObject.put("message", "Unauthorized access of protected resource, invalid credentials");
//            errorObject.put("error", HttpStatus.UNAUTHORIZED);
//            errorObject.put("code", errorCode);
//            errorObject.put("timestamp", new Timestamp(new Date().getTime()));
//            httpServletResponse.setContentType("application/json;charset=UTF-8");
//            httpServletResponse.setStatus(errorCode);
//            httpServletResponse.getWriter().write(mapper.writeValueAsString(errorObject));
//        };
//    }
//
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(restSecProps.getAllowedOrigins());
//        configuration.setAllowedMethods(restSecProps.getAllowedMethods());
//        configuration.setAllowedHeaders(restSecProps.getAllowedHeaders());
//        configuration.setAllowCredentials(restSecProps.isAllowCredentials());
//        configuration.setExposedHeaders(restSecProps.getExposedHeaders());
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.cors().configurationSource(corsConfigurationSource()).and().csrf().disable().formLogin().disable()
//                .httpBasic().disable().exceptionHandling().authenticationEntryPoint(restAuthenticationEntryPoint())
//                .and().authorizeRequests()
//                .requestMatchers(SecurityUtils::isFrameworkInternalRequestOrLogin).permitAll()
//                .antMatchers(restSecProps.getAllowedPublicApis().toArray(String[]::new)).permitAll()
//                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll().anyRequest().authenticated().and()
//                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//         Not using Spring CSRF here to be able to use plain HTML for the login page
        http.csrf().disable().formLogin().disable().headers().frameOptions().disable().and()

                // Register our CustomRequestCache, that saves unauthorized access attempts, so
                // the user is redirected after login.
                .requestCache().requestCache(new CustomRequestCache())

                // Restrict access to our application.
                .and().authorizeRequests()

                // Allow all Vaadin internal requests.
                .requestMatchers(SecurityUtils::isFrameworkInternalRequest).permitAll()

                // Allow all requests by logged in users.
                .anyRequest().authenticated()

                .and().addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);


        // Configure the login page.
//                .formLogin()
//                .loginPage(LOGIN_URL).permitAll()
//                .loginProcessingUrl(LOGIN_PROCESSING_URL)
//                .failureUrl(LOGIN_FAILURE_URL)
//
//                // Configure logout
//                .and().logout().logoutSuccessUrl(LOGOUT_SUCCESS_URL);
    }

//    @Override
//    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
//        authenticationManagerBuilder
//                .userDetailsService(customUserDetailsService)
//                .passwordEncoder(passwordEncoder());
//    }
////
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }

    /**
     * Allows access to static resources, bypassing Spring security.
     */
    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers(
                // Client-side JS
                "/VAADIN/**",

                // the standard favicon URI
                "/favicon.ico",

                // the robots exclusion standard
                "/robots.txt",

                // web application manifest
                "/manifest.webmanifest",
                "/sw.js",
                "/offline.html",

                // icons and images
                "/icons/**",
                "/images/**",
                "/styles/**",

                // (development mode) H2 debugging console
                "/h2-console/**");
    }
}