package com.inn.cafe.JWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customerUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // NOTE: CorsConfiguration.applyPermitDefaultValues() only allows GET/HEAD/POST -
        // every PUT/DELETE endpoint added across later modules (Coupons, Delivery, Store,
        // Bill status/cancel, Address update/delete, Cart update, etc.) would silently fail
        // the browser's CORS preflight (OPTIONS) request in real browsers, even though
        // server-side tools like PowerShell's Invoke-RestMethod never exercise CORS at all
        // and so never surfaced this. Explicitly allow the full REST verb set instead.
        http.cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration configuration = new CorsConfiguration().applyPermitDefaultValues();
                    configuration.setAllowedMethods(java.util.List.of("GET", "HEAD", "POST", "PUT", "DELETE", "PATCH"));
                    return configuration;
                }))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/user/login", "/user/signup", "/user/forgotPassword",
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .exceptionHandling(exceptionHandling -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
